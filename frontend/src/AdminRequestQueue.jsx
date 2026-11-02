import { useEffect, useState } from 'react';
import { FaHandsHelping, FaMapMarkerAlt } from 'react-icons/fa';
import api from './api.js';

const failure = error => error?.response?.data?.message || 'Could not load the request queue.';
export default function AdminRequestQueue({ assignments = false }) {
  const [requests, setRequests] = useState([]);
  const [volunteers, setVolunteers] = useState([]);
  const [selection, setSelection] = useState({});
  const [filter, setFilter] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  async function load() {
    setLoading(true);
    try {
      const [r, v] = await Promise.all([
        api.get('/admin/requests'), assignments ? api.get('/admin/volunteers/availability') : Promise.resolve({ data: [] }),
      ]);
      setRequests(r.data); setVolunteers(v.data); setError('');
    }
    catch (e) { setError(failure(e)); }
    finally { setLoading(false); }
  }
  useEffect(() => { load(); }, [assignments]);
  const shown = filter === 'ALL' ? requests : requests.filter(request => request.status === filter);
  async function assign(requestId) {
    setError(''); setNotice('');
    try { await api.post(`/admin/requests/${requestId}/assignments`, { volunteerId: Number(selection[requestId]) }); setNotice('Volunteer assigned to the request.'); await load(); }
    catch (e) { setError(e?.response?.data?.message || 'Assignment failed. Select an available volunteer.'); }
  }
  async function cancel(assignmentId) { setError(''); setNotice(''); try { await api.put(`/admin/assignments/${assignmentId}/status`, {status:'CANCELLED'}); setNotice('Assignment cancelled; the request is back in the queue.'); await load(); } catch(e) { setError(e?.response?.data?.message || 'Could not cancel assignment.'); } }
  const available = volunteers.filter(v => v.status === 'AVAILABLE');
  return <><p className="eyebrow">ADMINISTRATION</p><h1 className="dashboard-title">{assignments ? 'Task assignments' : 'Emergency requests'}</h1><p className="dashboard-subtitle">{assignments ? 'Assign available volunteers to requests and manage active assignments.' : 'Review incoming disaster requests and their current status.'}</p>
    {error && <div className="workspace-alert error" role="alert">{error}</div>}{notice && <div className="workspace-alert" role="status">{notice}</div>}
    <section className="workspace-list-card admin-request-queue"><div className="list-card-heading"><div><h2>{assignments ? 'Assignment queue' : 'Incoming requests'}</h2><p>{assignments ? `${requests.filter(r => !['COMPLETED', 'REJECTED'].includes(r.status)).length} requests can receive assignment updates` : `${requests.filter(r => ['PENDING', 'REVIEWING'].includes(r.status)).length} requests need review`}</p></div><select className="form-select availability-filter" aria-label="Filter request status" value={filter} onChange={e => setFilter(e.target.value)}><option value="ALL">All requests</option>{['PENDING','REVIEWING','ASSIGNED','IN_PROGRESS','COMPLETED','REJECTED'].map(s => <option key={s} value={s}>{s.replace('_',' ')}</option>)}</select></div>
      {loading ? <p className="empty-state">Loading request queue…</p> : shown.length === 0 ? <p className="empty-state">No requests in this view.</p> : <div className="request-list-items">{shown.map(request => <article className="request-card" key={request.id}>
        <div className="request-card-top"><div><span className={`urgency-tag ${request.urgency.toLowerCase()}`}>{request.urgency}</span><h3>{request.title}</h3></div><span className={`request-status ${request.status.toLowerCase()}`}>{request.status.replace('_', ' ')}</span></div>
        <p>{request.description}</p><div className="request-meta"><span><FaMapMarkerAlt /> {request.location}</span><span>From {request.requesterName} · {request.requesterEmail}</span></div>
        {request.requiredSkills?.length > 0 && <div className="request-tags">{request.requiredSkills.map(skill => <span key={skill.id}>{skill.name}</span>)}</div>}
        {assignments && request.assignments?.length > 0 && <div className="assigned-volunteers"><strong>Assigned volunteers</strong>{request.assignments.map(a => <span className="assigned-volunteer-item" key={a.id}>{a.volunteerName} · {a.status.replace('_',' ')}{!['COMPLETED','CANCELLED'].includes(a.status) && <button type="button" className="text-action" onClick={()=>cancel(a.id)}>Cancel</button>}</span>)}</div>}
        {assignments && !['COMPLETED','REJECTED'].includes(request.status) && <div className="assignment-controls"><select className="form-select auth-input" aria-label={`Volunteer for ${request.title}`} value={selection[request.id] || ''} onChange={e => setSelection({ ...selection, [request.id]: e.target.value })}><option value="">Choose available volunteer</option>{available.map(v => <option key={v.volunteerId} value={v.volunteerId}>{v.fullName} · {v.address || 'Location not set'}</option>)}</select><button className="button button-dark" disabled={!selection[request.id]} onClick={() => assign(request.id)}><FaHandsHelping /> Assign</button></div>}
      </article>)}</div>}
    </section>
  </>;
}
