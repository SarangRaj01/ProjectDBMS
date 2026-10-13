import React, { useState, useEffect } from 'react';
import { Package, Warehouse, ShoppingCart, AlertCircle, CheckCircle2, RotateCcw, Plus, RefreshCw } from 'lucide-react';

export default function App() {
  const [activeTab, setActiveTab] = useState('inventory');
  const [products, setProducts] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [inventory, setInventory] = useState([]);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  // Order modal state
  const [isOrderModalOpen, setIsOrderModalOpen] = useState(false);
  const [selectedProductId, setSelectedProductId] = useState('');
  const [selectedWarehouseId, setSelectedWarehouseId] = useState('');
  const [orderQuantity, setOrderQuantity] = useState(1);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    fetchAllData();
  }, []);

  async function fetchAllData() {
    setLoading(true);
    setErrorMsg('');
    try {
      const [prodRes, whRes, invRes, ordRes] = await Promise.all([
        fetch('/api/products'),
        fetch('/api/warehouses'),
        fetch('/api/inventory'),
        fetch('/api/orders')
      ]);

      if (prodRes.ok) setProducts(await prodRes.json());
      if (whRes.ok) setWarehouses(await whRes.json());
      if (invRes.ok) setInventory(await invRes.json());
      if (ordRes.ok) setOrders(await ordRes.json());
    } catch (err) {
      setErrorMsg('Failed to connect to backend server. Ensure Spring Boot is running on port 8080.');
    } finally {
      setLoading(false);
    }
  }

  async function handleCreateOrder(e) {
    e.preventDefault();
    setIsSubmitting(true);
    setErrorMsg('');
    setSuccessMsg('');

    // Generate unique idempotency key
    const idempotencyKey = 'ORD-' + Date.now() + '-' + Math.random().toString(36).substring(2, 7);

    const payload = {
      customerId: 1, // Default to seeded customer 'Aarav Nair'
      idempotencyKey,
      items: [
        {
          productId: Number(selectedProductId),
          warehouseId: Number(selectedWarehouseId),
          quantity: Number(orderQuantity)
        }
      ]
    };

    try {
      const res = await fetch('/api/orders', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      const data = await res.json();
      if (!res.ok) {
        throw new Error(data.message || data.error || 'Failed to place order');
      }

      setSuccessMsg(`Order #${data.orderId} placed successfully! Stock reserved atomically.`);
      setIsOrderModalOpen(false);
      fetchAllData();
    } catch (err) {
      setErrorMsg(err.message);
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleCancelOrder(orderId) {
    if (!window.confirm(`Are you sure you want to cancel Order #${orderId}? Reserved stock will be returned to inventory.`)) {
      return;
    }

    setErrorMsg('');
    setSuccessMsg('');
    try {
      const res = await fetch(`/api/orders/${orderId}/cancel?reason=CustomerCancellation`, {
        method: 'POST'
      });
      const data = await res.json();
      if (!res.ok) {
        throw new Error(data.message || data.error || 'Failed to cancel order');
      }

      setSuccessMsg(`Order #${orderId} cancelled. Reserved stock released exactly once.`);
      fetchAllData();
    } catch (err) {
      setErrorMsg(err.message);
    }
  }

  const totalOnHand = inventory.reduce((sum, item) => sum + item.onHandQuantity, 0);
  const totalReserved = inventory.reduce((sum, item) => sum + item.reservedQuantity, 0);
  const totalAvailable = totalOnHand - totalReserved;

  return (
    <div className="app-container">
      {/* Header */}
      <header className="header">
        <div>
          <div className="brand-title">
            <Package color="#3b82f6" size={28} />
            Distributed Inventory & Order Consistency
          </div>
          <p style={{ color: 'var(--text-muted)', fontSize: '13px', marginTop: '4px' }}>
            ACID Transactions · Multi-Warehouse Atomic Stock Reservation · Row Locking
          </p>
        </div>
        <div style={{ display: 'flex', gap: '12px' }}>
          <button className="btn" onClick={fetchAllData} style={{ background: 'var(--bg-card)', color: '#fff' }}>
            <RefreshCw size={15} /> Refresh
          </button>
          <button className="btn btn-primary" onClick={() => {
            if (products.length > 0) setSelectedProductId(products[0].productId);
            if (warehouses.length > 0) setSelectedWarehouseId(warehouses[0].warehouseId);
            setIsOrderModalOpen(true);
          }}>
            <Plus size={16} /> Place New Order
          </button>
        </div>
      </header>

      {/* Global Alerts */}
      {errorMsg && (
        <div className="alert alert-error">
          <AlertCircle size={16} style={{ display: 'inline', marginRight: '8px', verticalAlign: 'text-bottom' }} />
          {errorMsg}
        </div>
      )}
      {successMsg && (
        <div className="alert alert-success">
          <CheckCircle2 size={16} style={{ display: 'inline', marginRight: '8px', verticalAlign: 'text-bottom' }} />
          {successMsg}
        </div>
      )}

      {/* Metrics Banner */}
      <div className="metrics-grid">
        <div className="metric-card blue">
          <div className="metric-label">TOTAL PRODUCTS</div>
          <div className="metric-value">{products.length}</div>
        </div>
        <div className="metric-card green">
          <div className="metric-label">AVAILABLE STOCK (UNITS)</div>
          <div className="metric-value">{totalAvailable}</div>
        </div>
        <div className="metric-card amber">
          <div className="metric-label">RESERVED STOCK (UNITS)</div>
          <div className="metric-value">{totalReserved}</div>
        </div>
        <div className="metric-card indigo">
          <div className="metric-label">TOTAL ORDERS</div>
          <div className="metric-value">{orders.length}</div>
        </div>
      </div>

      {/* Navigation Tabs */}
      <div className="tabs-nav">
        <button
          className={`tab-btn ${activeTab === 'inventory' ? 'active' : ''}`}
          onClick={() => setActiveTab('inventory')}
        >
          🏭 Warehouse Inventory
        </button>
        <button
          className={`tab-btn ${activeTab === 'products' ? 'active' : ''}`}
          onClick={() => setActiveTab('products')}
        >
          📦 Product Catalog
        </button>
        <button
          className={`tab-btn ${activeTab === 'orders' ? 'active' : ''}`}
          onClick={() => setActiveTab('orders')}
        >
          🛒 Orders & Reservations
        </button>
      </div>

      {/* Tab: Inventory */}
      {activeTab === 'inventory' && (
        <div className="panel">
          <div className="panel-header">
            <h2 className="panel-title">Real-Time Inventory State by Warehouse</h2>
            <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
              Invariant: Available = On Hand - Reserved
            </span>
          </div>
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Product</th>
                  <th>SKU</th>
                  <th>Warehouse</th>
                  <th>On-Hand</th>
                  <th>Reserved</th>
                  <th>Available</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {inventory.map((item) => (
                  <tr key={item.inventoryId}>
                    <td><strong>{item.productName}</strong></td>
                    <td><code>{item.productSku}</code></td>
                    <td>{item.warehouseName}</td>
                    <td>{item.onHandQuantity}</td>
                    <td style={{ color: item.reservedQuantity > 0 ? '#f59e0b' : 'inherit' }}>
                      {item.reservedQuantity}
                    </td>
                    <td>
                      <strong style={{ color: item.availableQuantity > 0 ? '#34d399' : '#ef4444' }}>
                        {item.availableQuantity}
                      </strong>
                    </td>
                    <td>
                      <span className={`badge ${item.availableQuantity > 0 ? 'active' : 'cancelled'}`}>
                        {item.availableQuantity > 0 ? 'IN STOCK' : 'OUT OF STOCK'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab: Products */}
      {activeTab === 'products' && (
        <div className="panel">
          <div className="panel-header">
            <h2 className="panel-title">Master Product Catalog</h2>
          </div>
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>SKU</th>
                  <th>Product Name</th>
                  <th>Category</th>
                  <th>Unit Price</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {products.map((p) => (
                  <tr key={p.productId}>
                    <td>#{p.productId}</td>
                    <td><code>{p.sku}</code></td>
                    <td><strong>{p.productName}</strong></td>
                    <td>{p.categoryName || 'General'}</td>
                    <td>₹{Number(p.unitPrice).toFixed(2)}</td>
                    <td>
                      <span className={`badge ${p.status === 'ACTIVE' ? 'active' : 'cancelled'}`}>
                        {p.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab: Orders */}
      {activeTab === 'orders' && (
        <div className="panel">
          <div className="panel-header">
            <h2 className="panel-title">Order Processing & Reservation History</h2>
          </div>
          {orders.length === 0 ? (
            <p style={{ color: 'var(--text-muted)', textAlign: 'center', padding: '32px' }}>
              No orders placed yet. Click <strong>Place New Order</strong> above to create one.
            </p>
          ) : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Order ID</th>
                    <th>Customer</th>
                    <th>Idempotency Key</th>
                    <th>Items</th>
                    <th>Total</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {orders.map((ord) => (
                    <tr key={ord.orderId}>
                      <td><strong>#{ord.orderId}</strong></td>
                      <td>{ord.customerName || 'Customer #' + ord.customerId}</td>
                      <td><code>{ord.idempotencyKey}</code></td>
                      <td>
                        {ord.items?.map((it, idx) => (
                          <div key={idx} style={{ fontSize: '12px' }}>
                            {it.productName} × {it.quantity} (₹{it.unitPrice})
                          </div>
                        ))}
                      </td>
                      <td><strong>₹{Number(ord.totalAmount).toFixed(2)}</strong></td>
                      <td>
                        <span className={`badge ${ord.status.toLowerCase()}`}>
                          {ord.status}
                        </span>
                      </td>
                      <td>
                        {ord.status === 'CONFIRMED' && (
                          <button
                            className="btn btn-danger"
                            onClick={() => handleCancelOrder(ord.orderId)}
                          >
                            <RotateCcw size={12} /> Cancel & Release
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Place Order Modal */}
      {isOrderModalOpen && (
        <div className="modal-overlay">
          <div className="modal">
            <h3 style={{ marginBottom: '16px', fontSize: '18px' }}>Place New Order</h3>
            <p style={{ fontSize: '13px', color: 'var(--text-muted)', marginBottom: '20px' }}>
              Submitting tests atomic stock reservation and verifies that available inventory is never over-reserved.
            </p>

            <form onSubmit={handleCreateOrder}>
              <div className="form-group">
                <label className="form-label">Select Product</label>
                {products.length === 0 ? (
                  <div style={{ color: 'var(--text-muted)', fontSize: '13px', padding: '8px 0' }}>
                    ⚠️ No products loaded. Please ensure the backend is running with your MySQL credentials.
                  </div>
                ) : (
                  <select
                    className="form-select"
                    value={selectedProductId}
                    onChange={(e) => setSelectedProductId(e.target.value)}
                    required
                  >
                    {products.map((p) => (
                      <option key={p.productId} value={p.productId}>
                        {p.productName} ({p.sku}) — ₹{p.unitPrice}
                      </option>
                    ))}
                  </select>
                )}
              </div>

              <div className="form-group">
                <label className="form-label">Select Warehouse</label>
                {warehouses.length === 0 ? (
                  <div style={{ color: 'var(--text-muted)', fontSize: '13px', padding: '8px 0' }}>
                    ⚠️ No warehouses loaded.
                  </div>
                ) : (
                  <select
                    className="form-select"
                    value={selectedWarehouseId}
                    onChange={(e) => setSelectedWarehouseId(e.target.value)}
                    required
                  >
                    {warehouses.map((w) => (
                      <option key={w.warehouseId} value={w.warehouseId}>
                        {w.warehouseName} ({w.location})
                      </option>
                    ))}
                  </select>
                )}
              </div>

              <div className="form-group">
                <label className="form-label">Quantity to Reserve</label>
                <input
                  type="number"
                  min="1"
                  className="form-input"
                  value={orderQuantity}
                  onChange={(e) => setOrderQuantity(e.target.value)}
                  disabled={products.length === 0}
                  required
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '24px' }}>
                <button
                  type="button"
                  className="btn"
                  onClick={() => setIsOrderModalOpen(false)}
                  style={{ background: 'transparent', color: 'var(--text-muted)' }}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={isSubmitting || products.length === 0 || warehouses.length === 0}
                >
                  {isSubmitting ? 'Reserving...' : 'Confirm Order'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
