import { useEffect, useState } from 'react';
import { FaSave } from 'react-icons/fa';
import { useAuth } from './AuthContext.jsx';
import api from './api.js';

const failure = error => error?.response?.data?.message || 'Could not save your profile. Check the backend connection and try again.';

export default function CommunityProfile({ admin = false }) {
  const { updateCurrentUser } = useAuth();
  const [form, setForm] = useState({ fullName: '', email: '', phone: '', address: '' });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  useEffect(() => {
    api.get(admin ? '/admin/me/profile' : '/user/me/profile').then(({ data }) => setForm({
      fullName: data.fullName || '', email: data.email || '', phone: data.phone || '', address: data.address || '',
    })).catch(requestError => setError(failure(requestError))).finally(() => setLoading(false));
  }, []);

  async function save(event) {
    event.preventDefault(); setSaving(true); setError(''); setNotice('');
    try {
      const { data } = await api.put(admin ? '/admin/me/profile' : '/user/me/profile', {
        fullName: form.fullName, email: form.email, phone: form.phone, address: form.address,
      });
      setForm({ ...data, address: data.address || '' });
      updateCurrentUser(data);
      setNotice('Your profile has been saved.');
    } catch (requestError) { setError(failure(requestError)); }
    finally { setSaving(false); }
  }

  return <>
    <p className="eyebrow">{admin ? 'ADMINISTRATION' : 'COMMUNITY SPACE'}</p>
    <h1 className="dashboard-title">My profile</h1>
    <p className="dashboard-subtitle">Keep your contact details current so your account information stays accurate.</p>
    {error && <div className="workspace-alert error" role="alert">{error}</div>}
    {notice && <div className="workspace-alert" role="status">{notice}</div>}
    <form className="workspace-form-card" onSubmit={save}>
      <div className="form-card-heading"><div><h2>Personal details</h2><p>Your email is your sign-in address. Updating it changes the address used to sign in.</p></div></div>
      <label className="form-label" htmlFor="community-name">Full name</label>
      <input id="community-name" className="form-control auth-input" required maxLength="120" value={form.fullName}
        onChange={event => setForm(current => ({ ...current, fullName: event.target.value }))} disabled={loading} />
      <label className="form-label mt-3" htmlFor="community-email">Email address</label>
      <input id="community-email" className="form-control auth-input" type="email" required maxLength="190" value={form.email}
        onChange={event => setForm(current => ({ ...current, email: event.target.value }))} disabled={loading} />
      <label className="form-label mt-3" htmlFor="community-phone">Phone</label>
      <input id="community-phone" className="form-control auth-input" type="tel" required minLength="7" maxLength="20" value={form.phone}
        onChange={event => setForm(current => ({ ...current, phone: event.target.value }))} disabled={loading} />
      <label className="form-label mt-3" htmlFor="community-address">Address / city</label>
      <input id="community-address" className="form-control auth-input" maxLength="500" value={form.address}
        onChange={event => setForm(current => ({ ...current, address: event.target.value }))} disabled={loading} />
      <button className="button button-dark mt-4" type="submit" disabled={loading || saving}>
        <FaSave /> {loading ? 'Loading profile…' : saving ? 'Saving…' : 'Save profile'}
      </button>
    </form>
  </>;
}
