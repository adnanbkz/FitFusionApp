const functions = require('firebase-functions');
const crypto    = require('crypto');
const https     = require('https');
const qs        = require('querystring');

const FATSECRET_URL = 'https://platform.fatsecret.com/rest/server.api';

// ─── OAuth 1.0a helpers ───────────────────────────────────────────────────────

function pct(str) {
  return encodeURIComponent(String(str))
    .replace(/!/g,'%21').replace(/'/g,'%27')
    .replace(/\(/g,'%28').replace(/\)/g,'%29').replace(/\*/g,'%2A');
}

function signedParams(extraParams, key, secret) {
  const oauth = {
    oauth_consumer_key:     key,
    oauth_nonce:            crypto.randomBytes(12).toString('hex'),
    oauth_signature_method: 'HMAC-SHA1',
    oauth_timestamp:        String(Math.floor(Date.now() / 1000)),
    oauth_version:          '1.0',
  };
  const all = { ...extraParams, ...oauth };
  const paramStr = Object.keys(all).sort()
    .map(k => `${pct(k)}=${pct(all[k])}`).join('&');
  const base = `GET&${pct(FATSECRET_URL)}&${pct(paramStr)}`;
  const sig  = crypto.createHmac('sha1', `${pct(secret)}&`).update(base).digest('base64');
  return { ...all, oauth_signature: sig };
}

function fatSecretGet(params, key, secret) {
  return new Promise((resolve, reject) => {
    const query = qs.stringify(signedParams(params, key, secret));
    https.get(`${FATSECRET_URL}?${query}`, res => {
      let buf = '';
      res.on('data', c => buf += c);
      res.on('end', () => {
        try { resolve(JSON.parse(buf)); } catch (e) { reject(e); }
      });
    }).on('error', reject);
  });
}

// Parses "Per 100g - Calories: 165kcal | Fat: 3.57g | Carbs: 0.00g | Protein: 31.02g"
function parseDesc(desc) {
  const n = s => parseFloat((desc.match(new RegExp(s + ':\\s*([\\d.]+)')) || [])[1] || '0');
  return { kcal: n('Calories'), fat: n('Fat'), carbs: n('Carbs'), protein: n('Protein') };
}

// ─── searchFoods ─────────────────────────────────────────────────────────────

exports.searchFoods = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Autenticacion requerida.');
  }

  const query = String(data.query || '').trim();
  if (!query) return { foods: [] };

  const maxResults = Math.min(Number(data.maxResults) || 20, 50);
  const key    = process.env.FATSECRET_KEY;
  const secret = process.env.FATSECRET_SECRET;

  let response;
  try {
    response = await fatSecretGet({
      method:            'foods.search',
      format:            'json',
      search_expression: query,
      max_results:       String(maxResults),
      page_number:       '0',
    }, key, secret);
  } catch (e) {
    console.error('FatSecret searchFoods error:', e);
    throw new functions.https.HttpsError('internal', 'Error al conectar con FatSecret.');
  }

  const rawList = response.foods?.food;
  if (!rawList) return { foods: [] };
  const list = Array.isArray(rawList) ? rawList : [rawList];

  const foods = list.map(f => {
    const n = parseDesc(f.food_description || '');
    return {
      fatSecretId:    f.food_id,
      name:           f.food_name,
      brand:          f.brand_name || null,
      kcalPer100g:    n.kcal,
      proteinPer100g: n.protein,
      carbsPer100g:   n.carbs,
      fatsPer100g:    n.fat,
      servingLabel:   '100g',
      servingGrams:   100.0,
    };
  });

  return { foods };
});

// ─── getFoodDetail ────────────────────────────────────────────────────────────

exports.getFoodDetail = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Autenticacion requerida.');
  }

  const foodId = String(data.foodId || '').trim();
  if (!foodId) {
    throw new functions.https.HttpsError('invalid-argument', 'foodId requerido.');
  }

  const key    = process.env.FATSECRET_KEY;
  const secret = process.env.FATSECRET_SECRET;

  let response;
  try {
    response = await fatSecretGet({
      method:  'food.get.v4',
      format:  'json',
      food_id: foodId,
    }, key, secret);
  } catch (e) {
    console.error('FatSecret getFoodDetail error:', e);
    throw new functions.https.HttpsError('internal', 'Error al conectar con FatSecret.');
  }

  const food = response.food;
  if (!food) {
    throw new functions.https.HttpsError('not-found', 'Alimento no encontrado.');
  }

  const servings = food.servings?.serving;
  const s = Array.isArray(servings) ? servings[0] : (servings || {});
  const metricAmount = parseFloat(s.metric_serving_amount || '100') || 100;
  const factor = 100 / metricAmount;

  return {
    fatSecretId:    food.food_id,
    name:           food.food_name,
    brand:          food.brand_name || null,
    kcalPer100g:    parseFloat(s.calories      || '0') * factor,
    proteinPer100g: parseFloat(s.protein        || '0') * factor,
    carbsPer100g:   parseFloat(s.carbohydrate   || '0') * factor,
    fatsPer100g:    parseFloat(s.fat            || '0') * factor,
    servingLabel:   s.serving_description || '100g',
    servingGrams:   metricAmount,
  };
});
