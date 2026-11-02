import { useEffect, useMemo, useState } from 'react';
import { FaClipboardList, FaMapMarkerAlt } from 'react-icons/fa';
import { useAuth } from './AuthContext.jsx';
import api from './api.js';

const failure = error => error?.response?.data?.message || 'Could not load volunteer history. Check that the latest DVC backend is running.';

function assignedItems(requests, email) {
  return requests.flatMap(request => (request.assignments || [])
    .filter(assignment => assignment.volunteerEmail?.toLowerCase() === email?.toLowerCase())
    .map(assignment => ({ ...request, assignment })))
    .sort((a, b) => new Date(b.assignment.assignedAt) - new Date(a.assignment.assignedAt));
}

function TaskCard({ item, children }) {
  return <article className="request-card">
    <div className="request-card-top"><div><span className={`urgency-tag ${item.urgency.toLowerCase()}`}>{item.urgency}</span><h3><FaClipboardList /> {item.title}</h3></div><span className={`request-status ${item.assignment.status.toLowerCase()}`}>{item.assignment.status.replace('_', ' ')}</span></div>
    <p>{item.description}</p><div className="request-meta"><span><FaMapMarkerAlt /> {item.location}</span><span>Assigned {item.assignment.assignedAt ? new Date(item.assignment.assignedAt).toLocaleString() : 'recently'}</span></div>
    {children}
  </article>;
}

export function VolunteerTaskHistoryPage() {
  const { user } = useAuth();
  const [requests, setRequests] = useState([]); const [error, setError] = useState(''); const [loading, setLoading] = useState(true);
  useEffect(() => { api.get('/volunteer/me/assignments').then(({ data }) => setRequests(data)).catch(e => setError(failure(e))).finally(() => setLoading(false)); }, []);
  const items = useMemo(() => assignedItems(requests, user?.email).filter(item => ['COMPLETED', 'CANCELLED'].includes(item.assignment.status)), [requests, user?.email]);
  return <><p className="eyebrow">VOLUNTEER SPACE</p><h1 className="dashboard-title">Task history</h1><p className="dashboard-subtitle">Completed and cancelled assignments are kept here for reference.</p>{error && <div className="workspace-alert error" role="alert">{error}</div>}<section className="workspace-list-card"><div className="list-card-heading"><div><h2>Past assignments</h2><p>Your completed response work.</p></div><span className="count-chip">{items.length}</span></div>{loading ? <p className="empty-state">Loading task history…</p> : !error && items.length === 0 ? <p className="empty-state">Completed tasks will appear here.</p> : <div className="request-list-items">{items.map(item => <TaskCard key={item.assignment.id} item={item} />)}</div>}</section></>;
}

export function VolunteerFeedbackPage() {
  const { user } = useAuth();
  const [requests, setRequests] = useState([]); const [feedback, setFeedback] = useState([]); const [ratings, setRatings] = useState({}); const [comments, setComments] = useState({}); const [error, setError] = useState(''); const [notice, setNotice] = useState(''); const [loading, setLoading] = useState(true); const [saving, setSaving] = useState(null);
  async function load() {
    setError('');
    try { const [assignmentResponse, feedbackResponse] = await Promise.all([api.get('/volunteer/me/assignments'), api.get('/volunteer/me/feedback')]); setRequests(assignmentResponse.data); setFeedback(feedbackResponse.data); }
    catch (e) { setError(failure(e)); }
    finally { setLoading(false); }
  }
  useEffect(() => { load(); }, []);
  const completed = useMemo(() => assignedItems(requests, user?.email).filter(item => item.assignment.status === 'COMPLETED'), [requests, user?.email]);
  const feedbackByAssignment = useMemo(() => new Map(feedback.map(item => [item.assignmentId, item])), [feedback]);
  async function submit(item) {
    setSaving(item.assignment.id); setError(''); setNotice('');
    try {
      await api.post(`/volunteer/me/assignments/${item.assignment.id}/feedback`, { rating: Number(ratings[item.assignment.id] || 5), comments: comments[item.assignment.id] || '' });
      setNotice(`Feedback saved for ${item.title}.`); await load();
    } catch (e) { setError(e?.response?.data?.message || 'Could not submit feedback. Please try again.'); }
    finally { setSaving(null); }
  }
  return <><p className="eyebrow">VOLUNTEER SPACE</p><h1 className="dashboard-title">Feedback</h1><p className="dashboard-subtitle">Share feedback on completed assignments and review what you have already sent.</p>{error && <div className="workspace-alert error" role="alert">{error}</div>}{notice && <div className="workspace-alert" role="status">{notice}</div>}<section className="workspace-list-card"><div className="list-card-heading"><div><h2>Completed assignments</h2><p>Feedback can be submitted once for each completed task.</p></div><span className="count-chip">{completed.length}</span></div>{loading ? <p className="empty-state">Loading feedback…</p> : !error && completed.length === 0 ? <p className="empty-state">Feedback options appear here after you complete an assigned task.</p> : <div className="request-list-items">{completed.map(item => { const saved = feedbackByAssignment.get(item.assignment.id); return <TaskCard key={item.assignment.id} item={item}>{saved ? <div className="feedback-form"><strong>Your feedback: {saved.rating}/5</strong><p>{saved.comments || 'No written comments.'}</p></div> : <div className="feedback-form"><strong>How was the response?</strong><select aria-label={`Rating for ${item.title}`} className="form-select auth-input" value={ratings[item.assignment.id] || 5} onChange={e => setRatings({ ...ratings, [item.assignment.id]: e.target.value })}>{[5, 4, 3, 2, 1].map(value => <option key={value} value={value}>{value} / 5</option>)}</select><input className="form-control auth-input" aria-label={`Comments for ${item.title}`} placeholder="Optional comments" value={comments[item.assignment.id] || ''} onChange={e => setComments({ ...comments, [item.assignment.id]: e.target.value })} /><button className="button button-outline" disabled={saving === item.assignment.id} onClick={() => submit(item)}>{saving === item.assignment.id ? 'Saving…' : 'Send feedback'}</button></div>}</TaskCard>; })}</div>}</section></>;
}
