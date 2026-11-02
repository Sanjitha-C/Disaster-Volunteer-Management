import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { FaCheck, FaClock, FaEdit, FaPlus, FaSave, FaTrashAlt, FaUserCircle } from 'react-icons/fa';
import api from './api.js';

function messageFrom(error) {
  return error?.response?.data?.message || 'The request could not be completed. Check the backend connection and try again.';
}

function Notice({ children, tone = 'success' }) {
  return <div className={`workspace-alert ${tone}`} role="status">{children}</div>;
}

export function VolunteerOverview() {
  const [profile, setProfile] = useState(null);
  const [skillCount, setSkillCount] = useState(0);
  const [error, setError] = useState('');
  useEffect(() => {
    Promise.all([api.get('/volunteer/me/profile'), api.get('/volunteer/me/skills')])
      .then(([profileResult, skillResult]) => { setProfile(profileResult.data); setSkillCount(skillResult.data.length); })
      .catch(requestError => setError(messageFrom(requestError)));
  }, []);

  return <>
    <p className="eyebrow">VOLUNTEER SPACE</p>
    <h1 className="dashboard-title">Welcome, {profile?.fullName?.split(' ')[0] || 'volunteer'}.</h1>
    <p className="dashboard-subtitle">Keep your profile and availability ready for coordination.</p>
    {error && <Notice tone="error">{error}</Notice>}
    <div className="welcome-banner volunteer-banner"><div><span className="banner-kicker">YOUR VOLUNTEER PROFILE</span>
      <h2>{profile ? `${profile.profileCompletionPercent}% profile complete` : 'Loading your profile…'}</h2>
      <p>Clear skills and availability help coordinators find the right support.</p>
    </div><div className="banner-mark"><FaUserCircle /></div></div>
    <div className="summary-grid">
      <article className="summary-card"><span className="summary-icon"><FaUserCircle /></span><span className="summary-label">PROFILE</span><strong>{profile?.profileCompletionPercent ?? '—'}% complete</strong><small>{profile?.address || 'Add your location'}</small></article>
      <article className="summary-card"><span className="summary-icon"><FaClock /></span><span className="summary-label">AVAILABILITY</span><strong>{profile?.availability || 'Loading'}</strong><small>Set your current status and time slots</small></article>
      <article className="summary-card"><span className="summary-icon"><FaCheck /></span><span className="summary-label">MY SKILLS</span><strong>{skillCount} {skillCount === 1 ? 'skill' : 'skills'}</strong><small>Skills help coordinators match requests</small></article>
    </div>
    <div className="workspace-note"><FaClock /><span><strong>Your volunteer workspace is connected.</strong> Review your <Link to="/dashboard/assignments">assigned tasks</Link> or browse your <Link to="/dashboard/history">task history</Link>.</span></div>
  </>;
}

export function VolunteerProfilePage() {
  const [form, setForm] = useState({ fullName: '', phone: '', address: '' });
  const [completion, setCompletion] = useState(0);
  const [busy, setBusy] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  useEffect(() => {
    api.get('/volunteer/me/profile').then(({ data }) => {
      setForm({ fullName: data.fullName || '', phone: data.phone || '', address: data.address || '' });
      setCompletion(data.profileCompletionPercent);
    }).catch(requestError => setError(messageFrom(requestError))).finally(() => setBusy(false));
  }, []);

  async function save(event) {
    event.preventDefault(); setBusy(true); setError(''); setNotice('');
    try {
      const { data } = await api.put('/volunteer/me/profile', form);
      setForm({ fullName: data.fullName, phone: data.phone, address: data.address || '' });
      setCompletion(data.profileCompletionPercent); setNotice('Your profile has been saved.');
    } catch (requestError) { setError(messageFrom(requestError)); }
    finally { setBusy(false); }
  }

  return <WorkspacePage title="My profile" description="Keep your contact details up to date for response coordination.">
    {error && <Notice tone="error">{error}</Notice>}{notice && <Notice>{notice}</Notice>}
    <form className="workspace-form-card" onSubmit={save}>
      <div className="form-card-heading"><div><h2>Personal details</h2><p>{completion}% of your profile is complete</p></div><span className="completion-chip">{completion}%</span></div>
      <label className="form-label" htmlFor="profile-name">Full name</label>
      <input id="profile-name" className="form-control auth-input" required maxLength="120" value={form.fullName}
        onChange={event => setForm(current => ({ ...current, fullName: event.target.value }))} />
      <label className="form-label mt-3" htmlFor="profile-phone">Phone</label>
      <input id="profile-phone" className="form-control auth-input" type="tel" required minLength="7" maxLength="20" value={form.phone}
        onChange={event => setForm(current => ({ ...current, phone: event.target.value }))} />
      <label className="form-label mt-3" htmlFor="profile-address">Address / city</label>
      <input id="profile-address" className="form-control auth-input" maxLength="500" value={form.address}
        onChange={event => setForm(current => ({ ...current, address: event.target.value }))} />
      <button className="button button-dark mt-4" type="submit" disabled={busy}><FaSave /> {busy ? 'Saving…' : 'Save profile'}</button>
    </form>
  </WorkspacePage>;
}

export function VolunteerSkillsPage() {
  const [skills, setSkills] = useState([]);
  const [name, setName] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  async function loadSkills() {
    try { const { data } = await api.get('/volunteer/me/skills'); setSkills(data); setError(''); }
    catch (requestError) { setError(messageFrom(requestError)); }
    finally { setLoading(false); }
  }
  useEffect(() => { loadSkills(); }, []);

  async function save(event) {
    event.preventDefault(); setBusy(true); setError(''); setNotice('');
    try {
      if (editingId) await api.put(`/volunteer/me/skills/${editingId}`, { name });
      else await api.post('/volunteer/me/skills', { name });
      setName(''); setEditingId(null); setNotice(editingId ? 'Skill updated.' : 'Skill added to your profile.'); await loadSkills();
    } catch (requestError) { setError(messageFrom(requestError)); }
    finally { setBusy(false); }
  }

  async function remove(skill) {
    setError(''); setNotice('');
    try { await api.delete(`/volunteer/me/skills/${skill.id}`); setNotice(`${skill.name} removed.`); await loadSkills(); }
    catch (requestError) { setError(messageFrom(requestError)); }
  }

  return <WorkspacePage title="My skills" description="Add skills you can use to support emergency response work.">
    {error && <Notice tone="error">{error}</Notice>}{notice && <Notice>{notice}</Notice>}
    <form className="inline-add-card" onSubmit={save}>
      <div className="add-card-copy"><span className="summary-icon"><FaPlus /></span><div><strong>{editingId ? 'Edit a skill' : 'Add a skill'}</strong><small>Use a short, clear skill name.</small></div></div>
      <div className="add-skill-controls"><input className="form-control auth-input" aria-label="Skill name" required maxLength="80" value={name} onChange={event => setName(event.target.value)} placeholder="e.g. First Aid" />
        <button className="button button-dark" disabled={busy}>{busy ? 'Saving…' : editingId ? 'Save skill' : 'Add skill'}</button>
        {editingId && <button className="button button-outline" type="button" onClick={() => { setEditingId(null); setName(''); }}>Cancel</button>}</div>
    </form>
    <section className="workspace-list-card"><div className="list-card-heading"><div><h2>Skills in your profile</h2><p>Coordinators use these to find suitable volunteers.</p></div><span className="count-chip">{skills.length}</span></div>
      {loading ? <p className="empty-state">Loading skills…</p> : skills.length === 0 ? <p className="empty-state">No skills added yet. Add your first skill above.</p> :
        <div className="skill-list">{skills.map(skill => <article className="skill-row" key={skill.id}><span className="skill-dot" /><strong>{skill.name}</strong><div className="row-actions">
          <button className="icon-action" type="button" aria-label={`Edit ${skill.name}`} onClick={() => { setEditingId(skill.id); setName(skill.name); setNotice(''); }}><FaEdit /></button>
          <button className="icon-action danger" type="button" aria-label={`Remove ${skill.name}`} onClick={() => remove(skill)}><FaTrashAlt /></button>
        </div></article>)}</div>}
    </section>
  </WorkspacePage>;
}

export function VolunteerAvailabilityPage() {
  const [availability, setAvailability] = useState('UNAVAILABLE');
  const [slots, setSlots] = useState([]);
  const [form, setForm] = useState({ startsAt: '', endsAt: '' });
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  async function loadAvailability() {
    try { const { data } = await api.get('/volunteer/me/availability'); setAvailability(data.status); setSlots(data.slots); setError(''); }
    catch (requestError) { setError(messageFrom(requestError)); }
    finally { setLoading(false); }
  }
  useEffect(() => { loadAvailability(); }, []);

  async function saveStatus() {
    setBusy(true); setError(''); setNotice('');
    try { const { data } = await api.put('/volunteer/me/availability', { status: availability }); setAvailability(data.status); setNotice('Availability status saved.'); }
    catch (requestError) { setError(messageFrom(requestError)); }
    finally { setBusy(false); }
  }

  async function saveSlot(event) {
    event.preventDefault(); setBusy(true); setError(''); setNotice('');
    try {
      const payload = { startsAt: form.startsAt, endsAt: form.endsAt };
      if (editingId) await api.put(`/volunteer/me/availability/slots/${editingId}`, payload);
      else await api.post('/volunteer/me/availability/slots', payload);
      setEditingId(null); setForm({ startsAt: '', endsAt: '' }); setNotice(editingId ? 'Time slot updated.' : 'Time slot added.'); await loadAvailability();
    } catch (requestError) { setError(messageFrom(requestError)); }
    finally { setBusy(false); }
  }

  async function removeSlot(slot) {
    setError(''); setNotice('');
    try { await api.delete(`/volunteer/me/availability/slots/${slot.id}`); setNotice('Time slot removed.'); await loadAvailability(); }
    catch (requestError) { setError(messageFrom(requestError)); }
  }

  return <WorkspacePage title="My availability" description="Set your current status and the dates and times when you can help.">
    {error && <Notice tone="error">{error}</Notice>}{notice && <Notice>{notice}</Notice>}
    <section className="workspace-form-card availability-status-card"><div className="form-card-heading"><div><h2>Current status</h2><p>Let coordinators know whether you can take an assignment.</p></div><span className={`availability-indicator ${availability.toLowerCase()}`} /></div>
      <div className="status-control"><select className="form-select auth-input" aria-label="Availability status" value={availability} onChange={event => setAvailability(event.target.value)}>
        <option value="AVAILABLE">Available</option><option value="UNAVAILABLE">Unavailable</option><option value="BUSY">Busy</option>
      </select><button className="button button-dark" type="button" disabled={busy} onClick={saveStatus}>{busy ? 'Saving…' : 'Save status'}</button></div>
    </section>
    <form className="workspace-form-card slot-form" onSubmit={saveSlot}><div className="form-card-heading"><div><h2>{editingId ? 'Edit time slot' : 'Add a time slot'}</h2><p>Use your local date and time. End must be after start.</p></div></div>
      <div className="row g-3"><div className="col-md-5"><label className="form-label" htmlFor="slot-start">Starts</label><input className="form-control auth-input" id="slot-start" type="datetime-local" required value={form.startsAt} onChange={event => setForm(current => ({ ...current, startsAt: event.target.value }))} /></div>
        <div className="col-md-5"><label className="form-label" htmlFor="slot-end">Ends</label><input className="form-control auth-input" id="slot-end" type="datetime-local" required value={form.endsAt} onChange={event => setForm(current => ({ ...current, endsAt: event.target.value }))} /></div>
        <div className="col-md-2 d-flex align-items-end"><button className="button button-dark w-100" disabled={busy}>{editingId ? 'Update' : 'Add slot'}</button></div></div>
      {editingId && <button type="button" className="text-action mt-2" onClick={() => { setEditingId(null); setForm({ startsAt: '', endsAt: '' }); }}>Cancel editing</button>}
    </form>
    <section className="workspace-list-card"><div className="list-card-heading"><div><h2>Scheduled availability</h2><p>Your saved dates and times.</p></div><span className="count-chip">{slots.length}</span></div>
      {loading ? <p className="empty-state">Loading time slots…</p> : slots.length === 0 ? <p className="empty-state">No time slots added yet.</p> : <div className="slot-list">{slots.map(slot => <article className="slot-row" key={slot.id}>
        <span className="slot-date"><FaClock /></span><span className="slot-times"><strong>{new Date(slot.startsAt).toLocaleDateString()}</strong><small>{new Date(slot.startsAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} – {new Date(slot.endsAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</small></span>
        <div className="row-actions"><button type="button" className="icon-action" aria-label="Edit time slot" onClick={() => { setEditingId(slot.id); setForm({ startsAt: slot.startsAt.slice(0, 16), endsAt: slot.endsAt.slice(0, 16) }); }}><FaEdit /></button>
          <button type="button" className="icon-action danger" aria-label="Remove time slot" onClick={() => removeSlot(slot)}><FaTrashAlt /></button></div>
      </article>)}</div>}
    </section>
  </WorkspacePage>;
}

export function WorkspacePage({ title, description, children }) {
  return <><p className="eyebrow">VOLUNTEER SPACE</p><h1 className="dashboard-title">{title}</h1><p className="dashboard-subtitle">{description}</p>{children}</>;
}
