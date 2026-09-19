// Frontend-only version: all data is stored in the browser (localStorage).
// Same functions as before, so no other file needs to change.

const KEY = 'inventory_data_v1';

const STANDARD = {
  kg: { family: 'kg', f: 1 }, g: { family: 'kg', f: 0.001 }, quintal: { family: 'kg', f: 100 },
  litre: { family: 'litre', f: 1 }, ml: { family: 'litre', f: 0.001 },
  piece: { family: 'piece', f: 1 }, dozen: { family: 'piece', f: 12 },
};
const ALIAS = {
  kilo: 'kg', kgs: 'kg', kilogram: 'kg', gram: 'g', grams: 'g', quintals: 'quintal',
  liter: 'litre', l: 'litre', litres: 'litre', pcs: 'piece', pc: 'piece', pieces: 'piece',
  dozens: 'dozen', bags: 'bag', cartons: 'carton', boxes: 'box',
};
const BASE_UNITS = ['kg', 'litre', 'piece'];

const norm = (u) => { const x = String(u).trim().toLowerCase(); return ALIAS[x] || x; };
const round = (v) => Math.round(v * 100) / 100;
const fail = (message, status = 400) => { const e = new Error(message); e.status = status; return e; };

// ---------- storage ----------
function seed() {
  const mk = (id, name, category, baseUnit, reorder, unit, qty, stock) => ({
    p: { id, name, category, baseUnit, reorderLevelBase: reorder, active: true,
         conversions: [{ unitName: unit, toBaseQty: qty }] },
    t: { id, productId: id, type: 'IN', quantity: stock, unit: baseUnit, qtyBase: stock,
         price: null, note: 'Opening stock', cancelled: false, createdAt: new Date().toISOString() },
  });
  const s = [
    mk(1, 'Rice', 'Grocery', 'kg', 25, 'bag', 25, 80),
    mk(2, 'Sugar', 'Grocery', 'kg', 20, 'bag', 50, 100),
    mk(3, 'Cooking Oil', 'Grocery', 'litre', 10, 'carton', 12, 8),
    mk(4, 'Soap', 'Household', 'piece', 24, 'carton', 48, 100),
  ];
  return { products: s.map((x) => x.p), txns: s.map((x) => x.t), nextProduct: 5, nextTxn: 5 };
}
function load() {
  try { const raw = localStorage.getItem(KEY); if (raw) return JSON.parse(raw); } catch (e) { /* ignore */ }
  const d = seed(); save(d); return d;
}
function save(d) { try { localStorage.setItem(KEY, JSON.stringify(d)); } catch (e) { /* ignore */ } }

// ---------- unit engine ----------
const currentStock = (d, pid) =>
  d.txns.filter((t) => t.productId === pid && !t.cancelled).reduce((s, t) => s + t.qtyBase, 0);

function toBase(p, unit, qty) {
  const u = norm(unit);
  if (u === p.baseUnit) return qty;
  const c = p.conversions.find((x) => x.unitName === u);
  if (c) return qty * c.toBaseQty;
  const s = STANDARD[u];
  if (s && s.family === p.baseUnit) return qty * s.f;
  throw fail(`No conversion for '${u}' on ${p.name}. Add it first.`);
}

function format(p, baseQty) {
  if (baseQty < 0) return '-' + format(p, -baseQty);
  let rem = round(baseQty);
  if (rem === 0) return `0 ${p.baseUnit}`;
  const big = p.conversions.filter((c) => c.toBaseQty > 1).sort((a, b) => b.toBaseQty - a.toBaseQty);
  const parts = [];
  for (const c of big) {
    const n = Math.floor(rem / c.toBaseQty + 1e-9);
    if (n > 0) { parts.push(`${n} ${c.unitName}`); rem = round(rem - n * c.toBaseQty); }
  }
  if (rem > 0 || parts.length === 0) parts.push(`${rem} ${p.baseUnit}`);
  return parts.join(' ');
}

function availableUnits(p) {
  const set = new Set(p.conversions.map((c) => c.unitName));
  set.add(p.baseUnit);
  Object.keys(STANDARD).filter((k) => STANDARD[k].family === p.baseUnit).sort().forEach((k) => set.add(k));
  return [...set];
}

function view(d, p) {
  const stock = currentStock(d, p.id);
  return {
    id: p.id, name: p.name, category: p.category, baseUnit: p.baseUnit,
    reorderLevelBase: p.reorderLevelBase, currentStockBase: stock,
    stockDisplay: format(p, stock),
    lowStock: p.reorderLevelBase > 0 && stock <= p.reorderLevelBase,
    outOfStock: stock <= 0,
    conversions: p.conversions, availableUnits: availableUnits(p),
  };
}

// ---------- API (same names as the backend version) ----------
export const api = {
  async products(q = '') {
    const d = load();
    return d.products
      .filter((p) => p.active && p.name.toLowerCase().includes(q.trim().toLowerCase()))
      .sort((a, b) => a.name.localeCompare(b.name))
      .map((p) => view(d, p));
  },

  async createProduct(b) {
    const d = load();
    const name = (b.name || '').trim();
    const baseUnit = norm(b.baseUnit || '');
    if (!name) throw fail('Product name is required');
    if (!BASE_UNITS.includes(baseUnit)) throw fail('Base unit must be kg, litre or piece');
    if (d.products.some((p) => p.name.toLowerCase() === name.toLowerCase()))
      throw fail(`Product '${name}' already exists`, 409);
    const conversions = [];
    for (const c of b.conversions || []) {
      const u = norm(c.unitName);
      if (u === baseUnit || STANDARD[u]) throw fail(`'${u}' is a standard unit. Use bag, carton or box.`);
      if (!(c.toBaseQty > 0)) throw fail('Conversion must be greater than 0');
      conversions.push({ unitName: u, toBaseQty: c.toBaseQty });
    }
    const p = { id: d.nextProduct++, name, category: b.category || '', baseUnit,
                reorderLevelBase: b.reorderLevel || 0, active: true, conversions };
    d.products.push(p);
    if (b.openingStock > 0) {
      d.txns.push({ id: d.nextTxn++, productId: p.id, type: 'IN', quantity: b.openingStock,
                    unit: baseUnit, qtyBase: b.openingStock, price: null, note: 'Opening stock',
                    cancelled: false, createdAt: new Date().toISOString() });
    }
    save(d);
    return view(d, p);
  },

  async deleteProduct(id) {
    const d = load();
    const p = d.products.find((x) => x.id === id);
    if (p) p.active = false;
    save(d);
    return null;
  },

  async recordStock(b) {
    const d = load();
    const p = d.products.find((x) => x.id === b.productId && x.active);
    if (!p) throw fail('Product not found', 404);
    if (!(b.quantity > 0)) throw fail('Quantity must be greater than 0');
    const base = toBase(p, b.unit, b.quantity);
    const signed = b.type === 'IN' ? base : -base;
    const current = currentStock(d, p.id);
    if (signed < 0 && current + signed < -1e-9 && !b.force)
      throw fail(`Only ${format(p, current)} of ${p.name} in stock.`, 409);
    const t = { id: d.nextTxn++, productId: p.id, type: b.type, quantity: b.quantity,
                unit: norm(b.unit), qtyBase: signed, price: b.price ?? null, note: b.note || '',
                cancelled: false, createdAt: new Date().toISOString() };
    d.txns.push(t);
    save(d);
    return { ...t, productName: p.name, stockNow: format(p, current + signed) };
  },

  async history() {
    const d = load();
    return [...d.txns].reverse().slice(0, 100).map((t) => ({
      ...t, productName: (d.products.find((p) => p.id === t.productId) || {}).name || '?',
    }));
  },

  async undo(id) {
    const d = load();
    const t = d.txns.find((x) => x.id === id);
    if (!t) throw fail('Transaction not found', 404);
    if (t.cancelled) throw fail('Already cancelled');
    if (currentStock(d, t.productId) - t.qtyBase < -1e-9)
      throw fail('Cannot undo: stock would go below zero', 409);
    t.cancelled = true;
    save(d);
    return t;
  },
};