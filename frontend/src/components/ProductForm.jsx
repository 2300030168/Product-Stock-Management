import { useState } from 'react';
import { api } from '../api';

export default function ProductForm({ onDone }) {
  const [name, setName] = useState('');
  const [category, setCategory] = useState('');
  const [baseUnit, setBaseUnit] = useState('kg');
  const [reorderLevel, setReorderLevel] = useState('');
  const [openingStock, setOpeningStock] = useState('');
  const [convs, setConvs] = useState([{ unitName: '', toBaseQty: '' }]);
  const [msg, setMsg] = useState('');

  const setConv = (i, field, value) =>
    setConvs(convs.map((c, idx) => (idx === i ? { ...c, [field]: value } : c)));

  const submit = async (e) => {
    e.preventDefault();
    try {
      await api.createProduct({
        name, category, baseUnit,
        reorderLevel: reorderLevel === '' ? 0 : Number(reorderLevel),
        openingStock: openingStock === '' ? 0 : Number(openingStock),
        conversions: convs
          .filter((c) => c.unitName && c.toBaseQty)
          .map((c) => ({ unitName: c.unitName, toBaseQty: Number(c.toBaseQty) })),
      });
      onDone();
    } catch (err) {
      setMsg(err.message);
    }
  };

  return (
    <form onSubmit={submit} className="card">
      <label>Product name
        <input value={name} onChange={(e) => setName(e.target.value)} required /></label>
      <label>Category
        <input value={category} onChange={(e) => setCategory(e.target.value)} /></label>
      <label>Stock is counted in
        <select value={baseUnit} onChange={(e) => setBaseUnit(e.target.value)}>
          <option value="kg">kg</option>
          <option value="litre">litre</option>
          <option value="piece">piece</option>
        </select></label>
      <label>Reorder level (in {baseUnit})
        <input type="number" min="0" step="any" value={reorderLevel}
               onChange={(e) => setReorderLevel(e.target.value)} /></label>
      <label>Opening stock (in {baseUnit})
        <input type="number" min="0" step="any" value={openingStock}
               onChange={(e) => setOpeningStock(e.target.value)} /></label>

      <h4>Trade units (optional)</h4>
      <p className="small">Example: 1 bag = 25 {baseUnit}</p>
      {convs.map((c, i) => (
        <div className="row" key={i}>
          <input placeholder="bag / carton / box" value={c.unitName}
                 onChange={(e) => setConv(i, 'unitName', e.target.value)} />
          <span>=</span>
          <input type="number" min="0" step="any" placeholder={`how many ${baseUnit}`}
                 value={c.toBaseQty} onChange={(e) => setConv(i, 'toBaseQty', e.target.value)} />
        </div>
      ))}
      <button type="button" className="link"
              onClick={() => setConvs([...convs, { unitName: '', toBaseQty: '' }])}>+ Add another unit</button>

      <button type="submit" className="primary">Save product</button>
      {msg && <div className="error">{msg}</div>}
    </form>
  );
}
