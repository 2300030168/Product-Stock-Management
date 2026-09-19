import { useCallback, useEffect, useState } from 'react';
import { api } from './api';
import ProductForm from './components/ProductForm';
import StockForm from './components/StockForm';
import History from './components/History';
import './App.css';

export default function App() {
  const [tab, setTab] = useState('stock');
  const [products, setProducts] = useState([]);
  const [q, setQ] = useState('');
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    try {
      setProducts(await api.products(q));
      setError('');
    } catch (e) {
      setError(e.message);
    }
  }, [q]);

  useEffect(() => { load(); }, [load]);

  const remove = async (p) => {
    if (!window.confirm(`Remove ${p.name}? History is kept.`)) return;
    try { await api.deleteProduct(p.id); load(); } catch (e) { setError(e.message); }
  };

  const status = (p) =>
    p.outOfStock ? <span className="badge red">Out of stock</span>
    : p.lowStock ? <span className="badge orange">Low</span>
    : <span className="badge green">OK</span>;

  const lowCount = products.filter((p) => p.lowStock || p.outOfStock).length;

  return (
    <div className="container">
      <h1>Inventory</h1>
      {lowCount > 0 && <div className="alert">{lowCount} item(s) low or out of stock</div>}
      {error && <div className="error">{error}</div>}

      <div className="tabs">
        {[['stock', 'Stock'], ['entry', 'Add / Remove Stock'], ['product', 'New Product'], ['history', 'History']]
          .map(([k, label]) => (
            <button key={k} className={tab === k ? 'active' : ''} onClick={() => setTab(k)}>{label}</button>
          ))}
      </div>

      {tab === 'stock' && (
        <>
          <input className="search" placeholder="Search product..." value={q}
                 onChange={(e) => setQ(e.target.value)} />
          <table>
            <thead>
              <tr><th>Product</th><th>Current stock</th><th>Reorder level</th><th>Status</th><th></th></tr>
            </thead>
            <tbody>
              {products.map((p) => (
                <tr key={p.id}>
                  <td>{p.name}<div className="small">{p.category}</div></td>
                  <td><b>{p.stockDisplay}</b></td>
                  <td>{p.reorderLevelBase} {p.baseUnit}</td>
                  <td>{status(p)}</td>
                  <td><button className="link" onClick={() => remove(p)}>Remove</button></td>
                </tr>
              ))}
              {products.length === 0 && <tr><td colSpan="5">No products found</td></tr>}
            </tbody>
          </table>
        </>
      )}

      {tab === 'entry' && <StockForm products={products} onDone={load} />}
      {tab === 'product' && <ProductForm onDone={() => { load(); setTab('stock'); }} />}
      {tab === 'history' && <History onChange={load} />}
    </div>
  );
}
