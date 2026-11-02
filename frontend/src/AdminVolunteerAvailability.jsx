import { useEffect, useMemo, useState } from 'react';
import { FaClock, FaHandsHelping } from 'react-icons/fa';
import api from './api.js';
import { WorkspacePage } from './VolunteerPages.jsx';

export default function AdminVolunteerAvailability() {
  const [volunteers, setVolunteers] = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    api.get('/admin/volunteers/availability').then(({ data }) => setVolunteers(data))
      .catch(requestError => setError(requestError?.response?.data?.message || 'Could not load volunteer availability.'))
      .finally(() => setLoading(false));
  }, []);
  const visible = useMemo(() => filter === 'ALL' ? volunteers : volunteers.filter(item => item.status === filter), [filter, volunteers]);
  const availableCount = volunteers.filter(item => item.status === 'AVAILABLE').length;

  return <WorkspacePage title="Volunteer availability" description="View active volunteer statuses and saved time slots.">
    {error && <div className="workspace-alert error" role="alert">{error}</div>}
    <div className="summary-grid admin-volunteer-summary">
      <article className="summary-card"><span className="summary-icon"><FaHandsHelping /></span><span className="summary-label">ACTIVE VOLUNTEERS</span><strong>{loading ? '—' : volunteers.length}</strong><small>Volunteer profiles</small></article>
      <article className="summary-card"><span className="summary-icon"><FaClock /></span><span className="summary-label">AVAILABLE NOW</span><strong>{loading ? '—' : availableCount}</strong><small>Current status is Available</small></article>
    </div>
    <section className="workspace-list-card admin-availability-list"><div className="list-card-heading"><div><h2>Volunteer list</h2><p>Availability information shared with coordinators.</p></div>
      <select className="form-select availability-filter" aria-label="Filter volunteers by availability" value={filter} onChange={event => setFilter(event.target.value)}>
        <option value="ALL">All statuses</option><option value="AVAILABLE">Available</option><option value="BUSY">Busy</option><option value="UNAVAILABLE">Unavailable</option>
      </select>
    </div>
    {loading ? <p className="empty-state">Loading volunteers…</p> : visible.length === 0 ? <p className="empty-state">No volunteers match this status.</p> :
      <div className="admin-volunteer-list">{visible.map(volunteer => <article className="admin-volunteer-row" key={volunteer.volunteerId}>
        <span className="avatar">{volunteer.fullName.slice(0, 1).toUpperCase()}</span><div className="admin-volunteer-main"><strong>{volunteer.fullName}</strong><small>{volunteer.email} · {volunteer.phone}</small><small>{volunteer.address || 'Address not provided'}</small>
          {volunteer.slots.length > 0 && <span className="admin-slot-summary"><FaClock /> {volunteer.slots.map(slot => `${new Date(slot.startsAt).toLocaleDateString()} ${new Date(slot.startsAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}–${new Date(slot.endsAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`).join(' · ')}</span>}
        </div><span className={`availability-badge ${volunteer.status.toLowerCase()}`}>{volunteer.status}</span>
      </article>)}</div>}
    </section>
  </WorkspacePage>;
}
