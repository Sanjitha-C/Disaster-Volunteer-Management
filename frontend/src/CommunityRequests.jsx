import { useEffect, useState } from 'react';
import { FaArrowRight, FaFirstAid, FaPlus } from 'react-icons/fa';
import api from './api.js';

const failure = error => error?.response?.data?.message || 'The request could not be completed. Check the backend connection.';

export default function CommunityRequests({ create = false }) {
  const [requests, setRequests] = useState([]);
  const [form, setForm] = useState({ title: '', description: '', location: '', urgency: 'MEDIUM', skills: '' });
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  async function load() {
    try { const { data } = await api.get('/user/requests'); setRequests(data); setError(''); }
    catch (e) { setError(failure(e)); }
    finally { setLoading(false); }
  }
  useEffect(() => { load(); }, []);
  async function submit(event) {
    event.preventDefault(); setBusy(true); setError(''); setNotice('');
    try {
      await api.post('/user/requests', { ...form, requiredSkills: form.skills.split(',').map(value => value.trim()).filter(Boolean) });
      setForm({ title: '', description: '', location: '', urgency: 'MEDIUM', skills: '' });
      setNotice('Your emergency request was submitted. You can track its status below.'); await load();
    } catch (e) { setError(failure(e)); }
    finally { setBusy(false); }
  }
  return <>
    <p className="eyebrow">COMMUNITY SPACE</p><h1 className="dashboard-title">{create ? 'Submit an emergency request' : 'My emergency requests'}</h1>
    <p className="dashboard-subtitle">Share clear details so the coordination team can respond.</p>
    {error && <div className="workspace-alert error" role="alert">{error}</div>}{notice && <div className="workspace-alert" role="status">{notice}</div>}
    {create && <form className="workspace-form-card request-form" onSubmit={submit}>
      <div className="form-card-heading"><div><h2>Request details</h2><p>Provide a location and urgency so responders can prioritize.</p></div><span className="completion-chip"><FaFirstAid /></span></div>
      <label className="form-label" htmlFor="request-title">What help is needed?</label><input id="request-title" className="form-control auth-input" required maxLength="160" value={form.title} onChange={e => setForm({ ...form, title: e.target.value })} placeholder="e.g. Need drinking water" />
      <label className="form-label mt-3" htmlFor="request-description">Details</label><textarea id="request-description" className="form-control auth-input request-description" required maxLength="5000" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} placeholder="Describe the situation and the help needed." />
      <div className="row g-3"><div className="col-md-7"><label className="form-label mt-3" htmlFor="request-location">Location</label><input id="request-location" className="form-control auth-input" required maxLength="255" value={form.location} onChange={e => setForm({ ...form, location: e.target.value })} placeholder="Address, neighborhood, or landmark" /></div>
        <div className="col-md-5"><label className="form-label mt-3" htmlFor="request-urgency">Urgency</label><select id="request-urgency" className="form-select auth-input" value={form.urgency} onChange={e => setForm({ ...form, urgency: e.target.value })}><option value="LOW">Low</option><option value="MEDIUM">Medium</option><option value="HIGH">High</option><option value="CRITICAL">Critical</option></select></div></div>
      <label className="form-label mt-3" htmlFor="request-skills">Helpful skills (optional)</label><input id="request-skills" className="form-control auth-input" value={form.skills} onChange={e => setForm({ ...form, skills: e.target.value })} placeholder="First Aid, Driving, Translation" /><small className="field-hint">Separate skills with commas.</small>
      <button className="button button-dark mt-4" disabled={busy}>{busy ? 'Submitting…' : 'Submit request'} <FaArrowRight /></button>
    </form>}
    <section className="workspace-list-card request-list"><div className="list-card-heading"><div><h2>{create ? 'Your submitted requests' : 'Request history'}</h2><p>Track the latest status from the response team.</p></div><span className="count-chip">{requests.length}</span></div>
      {loading ? <p className="empty-state">Loading requests…</p> : requests.length === 0 ? <p className="empty-state">No requests yet. Submit one when your community needs help.</p> : <div className="request-list-items">{requests.map(request => <article className="request-card" key={request.id}>
        <div className="request-card-top"><div><span className={`urgency-tag ${request.urgency.toLowerCase()}`}>{request.urgency}</span><h3>{request.title}</h3></div><span className={`request-status ${request.status.toLowerCase()}`}>{request.status.replace('_', ' ')}</span></div>
        <p>{request.description}</p><div className="request-meta"><span>⌖ {request.location}</span><span>{new Date(request.createdAt).toLocaleString()}</span></div>
        {request.requiredSkills?.length > 0 && <div className="request-tags">{request.requiredSkills.map(skill => <span key={skill.id}>{skill.name}</span>)}</div>}
        {request.assignments?.length > 0 && <div className="assigned-volunteers"><strong>Assigned support</strong>{request.assignments.map(a => <span key={a.id}>{a.volunteerName} · {a.status.replace('_', ' ')}</span>)}</div>}
      </article>)}</div>}
    </section>
  </>;
}
