#!/usr/bin/env node

const fs = require("node:fs");
const path = require("node:path");

const DEFAULT_INPUT = path.join(__dirname, "ingredients.seed.json");
const DEFAULT_COLLECTION = "ingredients";

const args = process.argv.slice(2);
const dryRun = args.includes("--dry-run");
const inputPath = readArg("--input") || DEFAULT_INPUT;
const collectionName = readArg("--collection") || DEFAULT_COLLECTION;

main().catch((error) => {
  console.error(error);
  process.exit(1);
});

async function main() {
  const products = readProducts(inputPath).map(toFirestoreIngredient);

  if (dryRun) {
    console.log(`Dry run OK: ${products.length} ingredientes listos para ${collectionName}.`);
    console.log(products.slice(0, 3));
    return;
  }

  const admin = require("firebase-admin");
  const projectId = process.env.FIREBASE_PROJECT_ID || process.env.GCLOUD_PROJECT || "fitfusiondiet";

  admin.initializeApp({ projectId });
  const db = admin.firestore();
  const timestamp = admin.firestore.FieldValue.serverTimestamp();

  let written = 0;
  for (const chunk of chunks(products, 450)) {
    const batch = db.batch();
    for (const product of chunk) {
      const ref = db.collection(collectionName).doc(product.id);
      batch.set(ref, { ...product.data, updatedAt: timestamp }, { merge: true });
      written += 1;
    }
    await batch.commit();
  }

  console.log(`Seed completado: ${written} ingredientes escritos en ${collectionName}.`);
}

function readArg(name) {
  const prefix = `${name}=`;
  return args.find((arg) => arg.startsWith(prefix))?.slice(prefix.length);
}

function readProducts(filePath) {
  const raw = fs.readFileSync(filePath, "utf8");
  const parsed = JSON.parse(raw);
  if (!Array.isArray(parsed)) {
    throw new Error(`El archivo ${filePath} debe ser un array JSON.`);
  }
  return parsed;
}

function toFirestoreIngredient(item) {
  const name = requiredString(item, "name");
  const id = item.id ? slugify(item.id) : slugify([item.brand, name].filter(Boolean).join(" "));
  const servingOptions = normalizeServings(item.servingOptions);
  const data = {
    name,
    nameLower: normalizeText(name),
    kcalPer100g: numberField(item, "kcalPer100g"),
    proteinPer100g: numberField(item, "proteinPer100g"),
    carbsPer100g: numberField(item, "carbsPer100g"),
    fatsPer100g: numberField(item, "fatsPer100g"),
    servingOptions,
    searchTokens: buildSearchTokens(item),
    source: item.source || "manual_seed",
    dataQualityScore: Number.isFinite(item.dataQualityScore) ? item.dataQualityScore : 0.7,
  };

  if (item.brand) data.brand = String(item.brand).trim();
  if (item.barcode) data.barcode = String(item.barcode).trim();
  if (item.categories) data.categories = item.categories.map(String);
  if (item.emoji) data.emoji = String(item.emoji);

  return { id, data };
}

function normalizeServings(servings) {
  const parsed = Array.isArray(servings)
    ? servings
        .map((serving) => ({
          label: String(serving.label || "").trim(),
          grams: Number(serving.grams),
        }))
        .filter((serving) => serving.label && Number.isFinite(serving.grams) && serving.grams > 0)
    : [];

  return parsed.length > 0 ? parsed : [{ label: "100g", grams: 100 }];
}

function buildSearchTokens(item) {
  const text = normalizeText([
    item.name,
    item.brand,
    ...(Array.isArray(item.categories) ? item.categories : []),
  ].filter(Boolean).join(" "));
  const words = text.split(" ").filter((word) => word.length >= 2);
  const tokens = new Set(words);

  for (let i = 0; i < words.length - 1; i += 1) {
    tokens.add(`${words[i]} ${words[i + 1]}`);
  }

  return Array.from(tokens).slice(0, 100);
}

function requiredString(item, field) {
  const value = String(item[field] || "").trim();
  if (!value) throw new Error(`Producto sin campo obligatorio: ${field}`);
  return value;
}

function numberField(item, field) {
  const value = Number(item[field]);
  if (!Number.isFinite(value)) throw new Error(`${item.name || "Producto"}: ${field} no es numerico.`);
  return value;
}

function normalizeText(value) {
  return String(value || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .trim()
    .replace(/\s+/g, " ");
}

function slugify(value) {
  return normalizeText(value)
    .replace(/[^a-z0-9]+/g, "_")
    .replace(/^_+|_+$/g, "")
    .slice(0, 120);
}

function chunks(values, size) {
  const result = [];
  for (let i = 0; i < values.length; i += size) {
    result.push(values.slice(i, i + size));
  }
  return result;
}
