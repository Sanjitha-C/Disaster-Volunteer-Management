import { useState } from 'react';
import { Link, Navigate, Route, Routes, useLocation, useNavigate, useParams } from 'react-router-dom';
import { FaArrowRight, FaBell, FaBoxes, FaChartPie, FaClipboardList, FaFirstAid, FaHandsHelping, FaHeart, FaLock, FaMapMarkedAlt, FaRegUserCircle, FaSignOutAlt, FaTasks, FaUsers, FaUserShield, FaUser, FaEye, FaEyeSlash } from 'react-icons/fa';
import { useAuth } from './AuthContext.jsx';
import api from './api.js';
import { VolunteerAvailabilityPage, VolunteerOverview, VolunteerProfilePage, VolunteerSkillsPage } from './VolunteerPages.jsx';
import AdminVolunteerAvailability from './AdminVolunteerAvailability.jsx';
import CommunityRequests from './CommunityRequests.jsx';
import CommunityProfile from './CommunityProfile.jsx';
import AdminRequestQueue from './AdminRequestQueue.jsx';
import VolunteerAssignments from './VolunteerAssignments.jsx';
import { VolunteerFeedbackPage, VolunteerTaskHistoryPage } from './VolunteerHistory.jsx';
import { AdminFeedbackPage, AdminResourcesPage, AdminReportsPage, AdminUserManagementPage, NotificationsPage } from './OperationsPages.jsx';

const demoAccounts = [
  { role: 'Admin', email: 'admin@gmail.com', password: 'Admin@12345' },
  { role: 'Volunteer', email: 'volunteer@gmail.com', password: 'Volunteer@12345' },
  { role: 'User', email: 'user@gmail.com', password: 'User@12345' },
];

function errorMessage(error) {
  return error?.response?.data?.message || 'The service could not be reached. Check that the DVC backend is running on port 8081.';
}

function Home() {
  const { user } = useAuth();
  return (
    <main className="landing container py-5">
      <nav className="d-flex justify-content-between align-items-center mb-5">
        <Link className="brand" to="/">DVC<span>.</span></Link>
        <div className="d-flex align-items-center gap-2">
          <span className="badge rounded-pill text-bg-light px-3 py-2">Stage 4 · Sign in</span>
          {user ? <Link className="button button-dark button-small" to="/dashboard">Dashboard <FaArrowRight /></Link>
            : <Link className="button button-outline button-small" to="/login">Sign in <FaArrowRight /></Link>}
        </div>
      </nav>
      <section className="hero row align-items-center g-5">
        <div className="col-lg-7">
          <p className="eyebrow">DISASTER VOLUNTEER COORDINATION</p>
          <h1>People ready to help.<br /><em>Connected when it matters.</em></h1>
          <p className="lead mt-4">A shared place for communities and volunteers to coordinate help during emergencies.</p>
          <div className="d-flex flex-wrap gap-3 mt-4">
            <Link className="button button-dark" to={user ? '/dashboard' : '/login'}>{user ? 'Open dashboard' : 'Sign in to your space'} <FaArrowRight /></Link>
            {!user && <><Link className="button button-outline" to="/register/requester">Request disaster help</Link><Link className="button button-outline" to="/register">Join as a volunteer</Link></>}
          </div>
          <div className="d-flex flex-wrap gap-3 mt-4">
            <span className="status-pill"><i /> Frontend online</span>
            <span className="status-pill muted">Secure role-based access</span>
          </div>
        </div>
        <div className="col-lg-5">
          <div className="feature-card">
            <div className="icon-box"><FaHandsHelping /></div>
            <h2>One place to coordinate</h2>
            <p>Sign in to your role-specific workspace. Request, volunteer, and coordination tools will appear as their project stages are completed.</p>
            <div className="feature-row"><FaMapMarkedAlt /><span>Local response coordination</span></div>
            <div className="feature-row"><FaHeart /><span>Volunteer-led support</span></div>
          </div>
        </div>
      </section>
      <footer className="mt-5 pt-4">Disaster Volunteer Coordination System <span>·</span> Community response workspace</footer>
    </main>
  );
}

function AuthFrame({ eyebrow, title, description, children, footer }) {
  return (
    <main className="auth-page">
      <Link className="auth-brand" to="/">DVC<span>.</span></Link>
      <section className="auth-card">
        <div className="auth-mark"><FaHandsHelping /></div>
        <p className="eyebrow mb-2">{eyebrow}</p>
        <h1 className="auth-title">{title}</h1>
        <p className="auth-description">{description}</p>
        {children}
        <div className="auth-footer">{footer}</div>
      </section>
      <p className="auth-caption">DISASTER VOLUNTEER COORDINATION SYSTEM</p>
    </main>
  );
}

function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  async function submit(event) {
    event.preventDefault(); setError(''); setBusy(true);
    try {
      await login(email, password);
      navigate(location.state?.from || '/dashboard', { replace: true });
    } catch (requestError) { setError(errorMessage(requestError)); }
    finally { setBusy(false); }
  }

  return (
    <AuthFrame eyebrow="WELCOME BACK" title="Sign in to DVC" description="Use your account to open your DVC workspace."
      footer={<>Need help? <Link to="/register/requester">Create a requester account</Link><br />Want to volunteer? <Link to="/register">Register as a volunteer</Link></>}>
      <form onSubmit={submit} className="auth-form">
        {error && <div className="form-alert" role="alert">{error}</div>}
        <label className="form-label" htmlFor="login-email">Email address</label>
        <input id="login-email" className="form-control auth-input" type="email" autoComplete="username" required
          placeholder="you@example.com" value={email} onChange={event => setEmail(event.target.value)} />
        <label className="form-label mt-3" htmlFor="login-password">Password</label>
        <div className="password-wrap">
          <input id="login-password" className="form-control auth-input" type={showPassword ? 'text' : 'password'}
            autoComplete="current-password" required placeholder="Enter your password" value={password}
            onChange={event => setPassword(event.target.value)} />
          <button className="password-toggle" type="button" aria-label={showPassword ? 'Hide password' : 'Show password'}
            onClick={() => setShowPassword(value => !value)}>{showPassword ? <FaEyeSlash /> : <FaEye />}</button>
        </div>
        <button className="button button-dark auth-submit" type="submit" disabled={busy}>
          {busy ? 'Signing in…' : 'Sign in'} <FaArrowRight />
        </button>
      </form>
      <div className="demo-note">
        <div className="demo-note-heading"><FaLock /> Demo accounts</div>
        <div className="demo-shortcuts">
          {demoAccounts.map(account => <button key={account.role} type="button" onClick={() => {
            setEmail(account.email); setPassword(account.password); setError('');
          }}>{account.role}</button>)}
        </div>
        <p>Choose a role to fill its local demo login.</p>
      </div>
    </AuthFrame>
  );
}

function RegisterPage({ requester = false }) {
  const { registerAccount } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ fullName: '', email: '', phone: '', password: '', address: '' });
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const update = event => setForm(current => ({ ...current, [event.target.name]: event.target.value }));

  async function submit(event) {
    event.preventDefault(); setError(''); setBusy(true);
    try { await registerAccount(form, requester); navigate('/dashboard', { replace: true }); }
    catch (requestError) { setError(errorMessage(requestError)); }
    finally { setBusy(false); }
  }

  return (
    <AuthFrame eyebrow={requester ? 'GET HELP' : 'JOIN THE RESPONSE'} title={requester ? 'Request help registration' : 'Volunteer registration'} description={requester ? 'Create an account to report a disaster need and track your requests.' : 'Create a volunteer account to join the DVC network.'}
      footer={<>Already registered? <Link to="/login">Sign in</Link><br />{requester ? <>Want to volunteer? <Link to="/register">Register as a volunteer</Link></> : <>Need help? <Link to="/register/requester">Create a requester account</Link></>}</>}>
      <form onSubmit={submit} className="auth-form">
        {error && <div className="form-alert" role="alert">{error}</div>}
        <label className="form-label" htmlFor="fullName">Full name</label>
        <input id="fullName" name="fullName" className="form-control auth-input" required maxLength="120" value={form.fullName} onChange={update} />
        <label className="form-label mt-3" htmlFor="register-email">Email address</label>
        <input id="register-email" name="email" className="form-control auth-input" type="email" autoComplete="email" required value={form.email} onChange={update} />
        <div className="row g-3 mt-0">
          <div className="col-sm-6"><label className="form-label mt-3" htmlFor="phone">Phone</label>
            <input id="phone" name="phone" className="form-control auth-input" type="tel" required minLength="7" maxLength="20" value={form.phone} onChange={update} /></div>
          <div className="col-sm-6"><label className="form-label mt-3" htmlFor="address">City / address</label>
            <input id="address" name="address" className="form-control auth-input" maxLength="500" value={form.address} onChange={update} /></div>
        </div>
        <label className="form-label mt-3" htmlFor="register-password">Password</label>
        <div className="password-wrap">
          <input id="register-password" name="password" className="form-control auth-input" type={showPassword ? 'text' : 'password'}
            autoComplete="new-password" required minLength="8" maxLength="72" value={form.password} onChange={update} />
          <button className="password-toggle" type="button" aria-label={showPassword ? 'Hide password' : 'Show password'}
            onClick={() => setShowPassword(value => !value)}>{showPassword ? <FaEyeSlash /> : <FaEye />}</button>
        </div>
        <p className="field-hint">At least 8 characters. Your account will be created with the {requester ? 'Community requester' : 'Volunteer'} role.</p>
        <button className="button button-dark auth-submit" type="submit" disabled={busy}>
          {busy ? 'Creating account…' : requester ? 'Create requester account' : 'Create volunteer account'} <FaArrowRight />
        </button>
      </form>
    </AuthFrame>
  );
}

const roleContent = {
  ADMIN: {
    label: 'Administration', icon: FaUserShield,
    links: [['Overview', 'overview', FaChartPie], ['User management', 'users', FaUsers], ['Volunteers', 'volunteers', FaHandsHelping], ['Emergency requests', 'requests', FaFirstAid], ['Task assignments', 'assignments', FaTasks], ['Resources', 'resources', FaBoxes], ['Notifications', 'notifications', FaBell], ['Feedback', 'feedback', FaHeart], ['Reports', 'reports', FaClipboardList], ['My profile', 'profile', FaRegUserCircle]],
  },
  VOLUNTEER: {
    label: 'Volunteer space', icon: FaHandsHelping,
    links: [['Overview', 'overview', FaChartPie], ['My profile', 'profile', FaRegUserCircle], ['My skills', 'skills', FaFirstAid], ['Availability', 'availability', FaHeart], ['Assigned tasks', 'assignments', FaTasks], ['Task history', 'history', FaClipboardList], ['Notifications', 'notifications', FaBell], ['Feedback', 'feedback', FaHeart]],
  },
  USER: {
    label: 'Community space', icon: FaUser,
    links: [['Overview', 'overview', FaChartPie], ['Submit emergency request', 'new-request', FaFirstAid], ['My requests', 'requests', FaClipboardList], ['Notifications', 'notifications', FaBell], ['My profile', 'profile', FaRegUserCircle]],
  },
};

function RequireAuth({ children }) {
  const { user, checkingSession } = useAuth();
  const location = useLocation();
  if (checkingSession) return <div className="app-loading"><span className="spinner-border spinner-border-sm" /> Checking your session…</div>;
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  return children;
}

function DashboardLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const { section = 'overview' } = useParams();
  const content = roleContent[user.role] || roleContent.USER;
  const activeItem = content.links.find(([, path]) => path === section) || content.links[0];
  const isOverview = section === 'overview' || !content.links.some(([, path]) => path === section);
  const RoleIcon = content.icon;

  async function signOut() {
    try { await logout(); } finally { navigate('/login', { replace: true }); }
  }

  return (
    <div className="dashboard-shell">
      <aside className="dashboard-sidebar">
        <Link className="brand sidebar-brand" to="/">DVC<span>.</span></Link>
        <div className="workspace-label"><span className="workspace-icon"><RoleIcon /></span><span>{content.label}<small>{user.role} workspace</small></span></div>
        <nav className="side-nav" aria-label={`${user.role} navigation`}>
          {content.links.map(([label, path, Icon]) => <Link key={path} to={`/dashboard/${path}`}
            className={`side-link ${section === path || (isOverview && path === 'overview') ? 'active' : ''}`}>
            <Icon /><span>{label}</span>
          </Link>)}
        </nav>
        <button className="side-link side-logout" type="button" onClick={signOut}><FaSignOutAlt /><span>Sign out</span></button>
        <div className="sidebar-account"><span className="avatar">{user.fullName?.slice(0, 1)?.toUpperCase() || 'D'}</span>
          <span className="account-text">{user.fullName}<small>{user.email}</small></span></div>
      </aside>
      <main className="dashboard-main">
        <header className="dashboard-topbar"><div className="crumb">DVC <span>/</span> {content.label}</div>
          <span className="topbar-user"><span className="online-dot" /> Account active</span></header>
        <section className="dashboard-content">
          {user.role === 'ADMIN' && isOverview ? <AdminReportsPage /> : user.role === 'ADMIN' && section === 'users' ? <AdminUserManagementPage currentUser={user} /> : user.role === 'VOLUNTEER' && isOverview ? <VolunteerOverview /> : user.role === 'VOLUNTEER' && section === 'profile' ? <VolunteerProfilePage /> : user.role === 'VOLUNTEER' && section === 'skills' ? <VolunteerSkillsPage /> : user.role === 'VOLUNTEER' && section === 'availability' ? <VolunteerAvailabilityPage /> : user.role === 'VOLUNTEER' && section === 'assignments' ? <VolunteerAssignments /> : user.role === 'VOLUNTEER' && section === 'history' ? <VolunteerTaskHistoryPage /> : user.role === 'VOLUNTEER' && section === 'feedback' ? <VolunteerFeedbackPage /> : user.role === 'VOLUNTEER' && section === 'notifications' ? <NotificationsPage /> : user.role === 'ADMIN' && section === 'profile' ? <CommunityProfile admin /> : user.role === 'ADMIN' && section === 'volunteers' ? <AdminVolunteerAvailability /> : user.role === 'ADMIN' && ['requests', 'assignments'].includes(section) ? <AdminRequestQueue assignments={section === 'assignments'} /> : user.role === 'ADMIN' && section === 'resources' ? <AdminResourcesPage /> : user.role === 'ADMIN' && section === 'feedback' ? <AdminFeedbackPage /> : user.role === 'ADMIN' && section === 'notifications' ? <NotificationsPage /> : user.role === 'USER' && section === 'new-request' ? <CommunityRequests create /> : user.role === 'USER' && ['requests', 'tracking'].includes(section) ? <CommunityRequests /> : user.role === 'USER' && section === 'notifications' ? <NotificationsPage /> : user.role === 'USER' && section === 'profile' ? <CommunityProfile /> : isOverview ? <>
            <p className="eyebrow">{content.label.toUpperCase()}</p>
            <h1 className="dashboard-title">Welcome, {user.fullName?.split(' ')[0] || 'there'}.</h1>
            <p className="dashboard-subtitle">Your DVC workspace is ready. The connected account and role are shown below.</p>
            <div className="welcome-banner"><div><span className="banner-kicker">YOUR WORKSPACE</span>
              <h2>{user.role === 'ADMIN' ? 'Coordination overview' : user.role === 'VOLUNTEER' ? 'Ready to make a difference?' : 'Community response, together.'}</h2>
              <p>{user.role === 'ADMIN' ? 'Manage the response as coordination features become available.' : user.role === 'VOLUNTEER' ? 'Your volunteer profile is connected to the response network.' : 'You can use this space to follow your emergency requests.'}</p>
            </div><div className="banner-mark"><RoleIcon /></div></div>
            <div className="summary-grid">
              <article className="summary-card"><span className="summary-icon"><FaRegUserCircle /></span><span className="summary-label">ACCOUNT</span><strong>{user.fullName}</strong><small>{user.email}</small></article>
              <article className="summary-card"><span className="summary-icon"><RoleIcon /></span><span className="summary-label">YOUR ROLE</span><strong>{user.role}</strong><small>Role-based access enabled</small></article>
              <article className="summary-card"><span className="summary-icon"><FaLock /></span><span className="summary-label">ACCOUNT STATUS</span><strong>{user.accountStatus}</strong><small>Signed in securely</small></article>
            </div>
            <div className="workspace-note"><FaClipboardList /><span><strong>Response tools are being added in stages.</strong> Request, assignment, and reporting data will appear here as their features are connected.</span></div>
          </> : <>
            <p className="eyebrow">{content.label.toUpperCase()}</p>
            <h1 className="dashboard-title">{activeItem[0]}</h1>
            <div className="coming-card"><span className="coming-icon"><FaClipboardList /></span>
              <h2>This section is on the DVC roadmap</h2>
              <p>This navigation item is connected. Its request and data workflow will be implemented in the matching project stage.</p>
              <Link className="button button-outline" to="/dashboard">Back to overview <FaArrowRight /></Link>
            </div>
          </>}
        </section>
      </main>
    </div>
  );
}

export default function App() {
  const { user, checkingSession } = useAuth();
  return <Routes>
    <Route path="/" element={<Home />} />
    <Route path="/login" element={!checkingSession && user ? <Navigate to="/dashboard" replace /> : <LoginPage />} />
    <Route path="/register" element={!checkingSession && user ? <Navigate to="/dashboard" replace /> : <RegisterPage />} />
    <Route path="/register/requester" element={!checkingSession && user ? <Navigate to="/dashboard" replace /> : <RegisterPage requester />} />
    <Route path="/dashboard/:section?" element={<RequireAuth><DashboardLayout /></RequireAuth>} />
    <Route path="*" element={<Navigate to="/" replace />} />
  </Routes>;
}
