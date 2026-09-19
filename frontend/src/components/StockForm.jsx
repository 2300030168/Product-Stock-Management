import { useState } from 'react';
import { api } from '../api';

export default function StockForm({ products, onDone }) {
  const [productId, setProductId] = useState('');
  const [type, setType] = useState('IN');
  const [quantity, setQuantity] = useState('');
  const [unit, setUnit] = useState('');
  const [price, setPrice] = useState('');
  const [msg, setMsg] = useState({ text: '', ok: true });

  const product = products.find((p) => String(p.id) === String(productId));

  const pickProduct = (id) => {
    setProductId(id);
    const p = products.find((x) => String(x.id) === String(id));
    setUnit(p ? p.availableUnits[0] : '');
  };

  const send = async (force = false) => {
    try {
      const r = await api.recordStock({
        productId: Number(productId), type, unit,
        quantity: Number(quantity),
        price: price === '' ? null : Number(price),
        force,
      });
      setMsg({ text: `Saved. ${r.productName} now: ${r.stockNow}`, ok: true });
      setQuantity(''); setPrice('');
      onDone();
    } catch (e) {
      // Stock would go below zero -> ask the user
      if (e.status === 409 && !force && window.confirm(e.message + '\nContinue anyway?')) {
        return send(true);
      }
      setMsg({ text: e.message, ok: false });
    }
  };

  const submit = (e) => { e.preventDefault(); send(false); };

  return (
    <form onSubmit={submit} className="card">
      <label>Product
        <select value={productId} onChange={(e) => pickProduct(e.target.value)} required>
          <option value="">Select...</option>
          {products.map((p) => <option key={p.id} value={p.id}>{p.name} ({p.stockDisplay})</option>)}
        </select></label>

      <div className="row">
        {[['IN', 'Stock In'], ['OUT', 'Sold / Out'], ['DAMAGED', 'Damaged']].map(([v, label]) => (
          <button type="button" key={v} className={type === v ? 'active' : ''} onClick={() => setType(v)}>
            {label}
          </button>
        ))}
      </div>

      <div className="row">
        <input type="number" min="0" step="any" placeholder="Quantity" value={quantity}
               onChange={(e) => setQuantity(e.target.value)} required />
        <select value={unit} onChange={(e) => setUnit(e.target.value)} required>
          {(product ? product.availableUnits : []).map((u) => <option key={u} value={u}>{u}</option>)}
        </select>
      </div>

      <label>Price per {unit || 'unit'} (optional)
        <input type="number" min="0" step="any" value={price} onChange={(e) => setPrice(e.target.value)} /></label>

      <button type="submit" className="primary">Save</button>
      {msg.text && <div className={msg.ok ? 'success' : 'error'}>{msg.text}</div>}
    </form>
  );
}
