const BASE = '/api';

async function req(path, options = {}) {
  const res = await fetch(BASE + path, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  if (res.status === 204) return null;
  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    const err = new Error(data.message || 'Something went wrong');
    err.status = res.status;
    throw err;
  }
  return data;
}

export const api = {
  products: (q = '') => req('/products?q=' + encodeURIComponent(q)),
  createProduct: (body) => req('/products', { method: 'POST', body: JSON.stringify(body) }),
  deleteProduct: (id) => req('/products/' + id, { method: 'DELETE' }),
  addConversion: (id, body) =>
    req(`/products/${id}/conversions`, { method: 'POST', body: JSON.stringify(body) }),
  recordStock: (body) => req('/stock', { method: 'POST', body: JSON.stringify(body) }),
  history: () => req('/stock/transactions'),
  undo: (id) => req(`/stock/transactions/${id}/undo`, { method: 'POST' }),
};
