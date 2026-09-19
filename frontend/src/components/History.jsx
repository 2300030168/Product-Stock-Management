import { useCallback, useEffect, useState } from 'react';
import { api } from '../api';

export default function History({ onChange }) {
  const [rows, setRows] = useState([]);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    try { setRows(await api.history()); } catch (e) { setError(e.message); }
  }, []);
  useEffect(() => { load(); }, [load]);

  const undo = async (id) => {
    try { await api.undo(id); setError(''); load(); onChange(); } catch (e) { setError(e.message); }
  };

  return (
    <>
      {error && <div className="error">{error}</div>}
      <table>
        <thead><tr><th>When</th><th>Product</th><th>Type</th><th>Quantity</th><th></th></tr></thead>
        <tbody>
          {rows.map((t) => (
            <tr key={t.id} className={t.cancelled ? 'cancelled' : ''}>
              <td>{new Date(t.createdAt).toLocaleString()}</td>
              <td>{t.productName}</td>
              <td>{t.type}</td>
              <td>{t.quantity} {t.unit}</td>
              <td>{t.cancelled ? 'Cancelled' : <button className="link" onClick={() => undo(t.id)}>Undo</button>}</td>
            </tr>
          ))}
          {rows.length === 0 && <tr><td colSpan="5">No transactions yet</td></tr>}
        </tbody>
      </table>
    </>
  );
}
