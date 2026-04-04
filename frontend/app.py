import streamlit as st
import requests
import pandas as pd
from datetime import datetime, date
import json

# ── Configuration ──────────────────────────────────────────────────────────────
BASE_URL = "http://localhost:8080/api"

st.set_page_config(
    page_title="Lost & Found System",
    page_icon="🔍",
    layout="wide",
    initial_sidebar_state="expanded",
)

# ── Helpers ────────────────────────────────────────────────────────────────────
def api(method, path, **kwargs):
    """Generic API call with error handling."""
    try:
        r = requests.request(method, f"{BASE_URL}{path}", timeout=10, **kwargs)
        r.raise_for_status()
        return r.json(), None
    except requests.exceptions.ConnectionError:
        return None, "⚠️ Cannot connect to the Spring backend. Make sure it's running on port 8080."
    except requests.exceptions.HTTPError as e:
        try:
            msg = e.response.json().get("error", str(e))
        except Exception:
            msg = str(e)
        return None, msg
    except Exception as e:
        return None, str(e)

def get_categories():
    data, _ = api("GET", "/categories")
    return data or []

def get_locations():
    data, _ = api("GET", "/locations")
    return data or []

def status_badge(status):
    colors = {
        "PENDING":  "#f0ad4e",
        "VERIFIED": "#5bc0de",
        "MATCHED":  "#9b59b6",
        "CLAIMED":  "#5cb85c",
        "CLOSED":   "#d9534f",
        "APPROVED": "#5cb85c",
        "REJECTED": "#d9534f",
    }
    color = colors.get(status, "#888")
    return f'<span style="background:{color};color:#fff;padding:2px 10px;border-radius:12px;font-size:0.78rem;font-weight:600;">{status}</span>'

def fmt_dt(val):
    if not val:
        return "—"
    try:
        return datetime.fromisoformat(str(val)).strftime("%d %b %Y, %H:%M")
    except Exception:
        return str(val)

# ── Session state ──────────────────────────────────────────────────────────────
if "user" not in st.session_state:
    st.session_state.user = None

# ── CSS ────────────────────────────────────────────────────────────────────────
st.markdown("""
<style>
[data-testid="stSidebar"] { background: #1a1f36; }
[data-testid="stSidebar"] * { color: #e2e8f0 !important; }
.card {
    background: #fff;
    border-radius: 12px;
    padding: 18px 20px;
    margin-bottom: 14px;
    border: 1px solid #e8eaf0;
    box-shadow: 0 1px 4px rgba(0,0,0,.06);
}
.card-title { font-size: 1.05rem; font-weight: 700; color: #1a1f36; margin-bottom: 4px; }
.card-meta  { font-size: 0.82rem; color: #6b7280; }
.metric-box {
    background: linear-gradient(135deg,#4f46e5,#7c3aed);
    color: #fff !important;
    border-radius: 12px;
    padding: 20px;
    text-align: center;
}
.metric-box h2 { font-size: 2.2rem; margin: 0; }
.metric-box p  { margin: 0; opacity: .85; font-size: .9rem; }
.section-header {
    font-size: 1.3rem; font-weight: 700; color: #1a1f36;
    border-left: 4px solid #4f46e5;
    padding-left: 10px; margin-bottom: 16px;
}
</style>
""", unsafe_allow_html=True)

# ══════════════════════════════════════════════════════════════════════════════
# SIDEBAR
# ══════════════════════════════════════════════════════════════════════════════
with st.sidebar:
    st.markdown("## 🔍 Lost & Found")
    st.markdown("---")

    if st.session_state.user:
        u = st.session_state.user
        st.markdown(f"**👤 {u['name']}**")
        st.markdown(f"`{u['role']}`")
        st.markdown("---")
        page_options = ["🏠 Dashboard", "📋 Browse Items", "➕ Report Item",
                        "📝 My Claims", "👤 My Profile"]
        if u["role"] == "ADMIN":
            page_options += ["🛡️ Admin Panel", "👥 Manage Users",
                             "🗂️ Categories & Locations"]
        page = st.radio("Navigate", page_options, label_visibility="collapsed")
        st.markdown("---")
        if st.button("🚪 Logout", use_container_width=True):
            st.session_state.user = None
            st.rerun()
    else:
        page = st.radio("Navigate", ["🔐 Login / Register"], label_visibility="collapsed")

    st.markdown("---")
    st.markdown(f"<small style='opacity:.5'>Backend: `{BASE_URL}`</small>", unsafe_allow_html=True)

# ══════════════════════════════════════════════════════════════════════════════
# PAGE: LOGIN / REGISTER
# ══════════════════════════════════════════════════════════════════════════════
if page == "🔐 Login / Register" or not st.session_state.user:
    st.title("🔍 Lost & Found Management System")
    tab_login, tab_reg = st.tabs(["Login", "Register"])

    with tab_login:
        with st.form("login_form"):
            email    = st.text_input("Email")
            password = st.text_input("Password", type="password")
            submitted = st.form_submit_button("Login", use_container_width=True)
        if submitted:
            data, err = api("POST", "/users/login",
                            json={"email": email, "password": password})
            if err:
                st.error(err)
            else:
                st.session_state.user = data.get("user", data)
                st.success("Logged in!")
                st.rerun()

    with tab_reg:
        with st.form("reg_form"):
            name     = st.text_input("Full Name")
            email    = st.text_input("Email", key="reg_email")
            phone    = st.text_input("Phone (10 digits)")
            password = st.text_input("Password", type="password", key="reg_pw")
            role     = st.selectbox("Role", ["USER", "ADMIN"])
            submitted = st.form_submit_button("Register", use_container_width=True)
        if submitted:
            data, err = api("POST", "/users/register",
                            json={"name": name, "email": email, "phone": phone,
                                  "password": password, "role": role})
            if err:
                st.error(err)
            else:
                st.success("Registered! Please log in.")

    st.stop()

# ══════════════════════════════════════════════════════════════════════════════
# PAGE: DASHBOARD
# ══════════════════════════════════════════════════════════════════════════════
elif page == "🏠 Dashboard":
    st.markdown('<div class="section-header">Dashboard</div>', unsafe_allow_html=True)

    lost_items,  _ = api("GET", "/items/lost")
    found_items, _ = api("GET", "/items/found")
    all_claims,  _ = api("GET", "/claims")
    all_users,   _ = api("GET", "/users")

    lost_items  = lost_items  or []
    found_items = found_items or []
    all_claims  = all_claims  or []
    all_users   = all_users   or []

    pending_lost  = [i for i in lost_items  if i.get("status") == "PENDING"]
    pending_found = [i for i in found_items if i.get("status") == "PENDING"]
    pending_claims = [c for c in all_claims if c.get("status") == "PENDING"]

    c1, c2, c3, c4 = st.columns(4)
    for col, label, val, icon in [
        (c1, "Lost Items",      len(lost_items),      "🔴"),
        (c2, "Found Items",     len(found_items),     "🟢"),
        (c3, "Pending Claims",  len(pending_claims),  "🟡"),
        (c4, "Total Users",     len(all_users),       "👥"),
    ]:
        col.markdown(f"""
        <div class="metric-box">
            <h2>{icon} {val}</h2>
            <p>{label}</p>
        </div>""", unsafe_allow_html=True)

    st.markdown("<br>", unsafe_allow_html=True)
    col_left, col_right = st.columns(2)

    with col_left:
        st.markdown("#### 🔴 Recent Lost Items")
        for item in lost_items[-5:][::-1]:
            cat  = item.get("category", {}) or {}
            loc  = item.get("location", {}) or {}
            st.markdown(f"""
            <div class="card">
                <div class="card-title">{item.get('title','—')}</div>
                <div class="card-meta">
                    📂 {cat.get('categoryName','—')} &nbsp;|&nbsp;
                    📍 {loc.get('placeName','—')} &nbsp;|&nbsp;
                    {status_badge(item.get('status',''))}
                </div>
            </div>""", unsafe_allow_html=True)

    with col_right:
        st.markdown("#### 🟢 Recent Found Items")
        for item in found_items[-5:][::-1]:
            cat = item.get("category", {}) or {}
            loc = item.get("location", {}) or {}
            st.markdown(f"""
            <div class="card">
                <div class="card-title">{item.get('title','—')}</div>
                <div class="card-meta">
                    📂 {cat.get('categoryName','—')} &nbsp;|&nbsp;
                    📍 {loc.get('placeName','—')} &nbsp;|&nbsp;
                    {status_badge(item.get('status',''))}
                </div>
            </div>""", unsafe_allow_html=True)

    # Status breakdown chart
    if lost_items or found_items:
        st.markdown("#### 📊 Items by Status")
        all_items = lost_items + found_items
        status_counts = pd.Series([i.get("status","UNKNOWN") for i in all_items]).value_counts()
        st.bar_chart(status_counts)

# ══════════════════════════════════════════════════════════════════════════════
# PAGE: BROWSE ITEMS
# ══════════════════════════════════════════════════════════════════════════════
elif page == "📋 Browse Items":
    st.markdown('<div class="section-header">Browse Items</div>', unsafe_allow_html=True)

    col_search, col_type = st.columns([3, 1])
    with col_search:
        keyword = st.text_input("🔍 Search items…", placeholder="e.g. iPhone, wallet, backpack")
    with col_type:
        item_type = st.selectbox("Type", ["All", "Lost", "Found"])

    categories = get_categories()
    locations  = get_locations()
    cat_names  = ["All"] + [c["categoryName"] for c in categories]
    loc_names  = ["All"] + [l["placeName"]    for l in locations]

    fc, fl = st.columns(2)
    with fc:
        sel_cat = st.selectbox("Category", cat_names)
    with fl:
        sel_loc = st.selectbox("Location",  loc_names)

    # Fetch
    if keyword.strip():
        items, err = api("GET", f"/items/search?keyword={keyword.strip()}")
    elif item_type == "Lost":
        items, err = api("GET", "/items/lost")
    elif item_type == "Found":
        items, err = api("GET", "/items/found")
    else:
        lost,  _ = api("GET", "/items/lost")
        found, _ = api("GET", "/items/found")
        items = (lost or []) + (found or [])
        err   = None

    if err:
        st.error(err)
        st.stop()

    items = items or []

    # Client-side filter
    if sel_cat != "All":
        items = [i for i in items if (i.get("category") or {}).get("categoryName") == sel_cat]
    if sel_loc != "All":
        items = [i for i in items if (i.get("location") or {}).get("placeName") == sel_loc]

    st.markdown(f"**{len(items)} item(s) found**")
    st.markdown("---")

    if not items:
        st.info("No items match your filters.")
    else:
        for item in items:
            cat  = item.get("category", {}) or {}
            loc  = item.get("location", {}) or {}
            rep  = item.get("reporter", {}) or {}
            itype = "🔴 LOST" if item.get("itemType") == "LOST" or "dateLost" in item else "🟢 FOUND"

            with st.expander(f"{itype} — {item.get('title','—')}  [{item.get('status','')}]"):
                c1, c2 = st.columns([2, 1])
                with c1:
                    st.markdown(f"**Description:** {item.get('description','—')}")
                    st.markdown(f"**Category:** {cat.get('categoryName','—')}")
                    st.markdown(f"**Location:** {loc.get('placeName','—')}")
                    if item.get("dateLost"):
                        st.markdown(f"**Date Lost:** {fmt_dt(item['dateLost'])}")
                    if item.get("dateFound"):
                        st.markdown(f"**Date Found:** {fmt_dt(item['dateFound'])}")
                    if item.get("rewardOffered"):
                        st.markdown(f"💰 **Reward:** {item['rewardOffered']}")
                    st.markdown(f"**Reported By:** {rep.get('name','—')}")
                    st.markdown(f"**Reported At:** {fmt_dt(item.get('dateReported'))}")
                with c2:
                    st.markdown(status_badge(item.get("status", "")), unsafe_allow_html=True)
                    if item.get("status") == "PENDING" and "FOUND" in itype:
                        if st.button("📩 File a Claim", key=f"claim_{item['itemId']}"):
                            st.session_state["claim_item"] = item
                            st.session_state["goto_claim"] = True
                            st.rerun()

    # Redirect if claim button was hit
    if st.session_state.get("goto_claim"):
        st.session_state["goto_claim"] = False
        st.info("👆 Go to **My Claims** in the sidebar to complete your claim.")

# ══════════════════════════════════════════════════════════════════════════════
# PAGE: REPORT ITEM
# ══════════════════════════════════════════════════════════════════════════════
elif page == "➕ Report Item":
    st.markdown('<div class="section-header">Report an Item</div>', unsafe_allow_html=True)

    categories = get_categories()
    locations  = get_locations()
    if not categories:
        st.warning("No categories found — make sure the Spring backend is running and seeded.")
        st.stop()

    tab_lost, tab_found = st.tabs(["🔴 Report Lost Item", "🟢 Report Found Item"])

    cat_map = {c["categoryName"]: c["categoryId"] for c in categories}
    loc_map = {l["placeName"]: l["locationId"]    for l in locations}

    with tab_lost:
        with st.form("lost_form"):
            title       = st.text_input("Item Title*", placeholder="e.g. Blue Nike Backpack")
            description = st.text_area("Description*", placeholder="Describe the item in detail…", height=100)
            col1, col2 = st.columns(2)
            with col1:
                cat_name = st.selectbox("Category*", list(cat_map.keys()), key="lcat")
                date_lost = st.date_input("Date Lost*", value=date.today())
            with col2:
                loc_name = st.selectbox("Location*", list(loc_map.keys()), key="lloc")
                reward   = st.text_input("Reward Offered (optional)", placeholder="e.g. ₹500")
            submitted = st.form_submit_button("🚨 Submit Lost Report", use_container_width=True)

        if submitted:
            if not title or not description:
                st.error("Title and description are required.")
            else:
                payload = {
                    "title": title,
                    "description": description,
                    "dateLost": datetime.combine(date_lost, datetime.min.time()).isoformat(),
                    "rewardOffered": reward or None,
                    "category": {"categoryId": cat_map[cat_name]},
                    "location": {"locationId": loc_map[loc_name]},
                }
                uid = st.session_state.user["userId"]
                data, err = api("POST", f"/items/lost?reporterId={uid}", json=payload)
                if err:
                    st.error(err)
                else:
                    st.success(f"✅ Lost item reported! ID: {data.get('itemId')}")

    with tab_found:
        with st.form("found_form"):
            title       = st.text_input("Item Title*", placeholder="e.g. iPhone 13 Pro")
            description = st.text_area("Description*", placeholder="Describe where and how you found it…", height=100)
            col1, col2 = st.columns(2)
            with col1:
                cat_name = st.selectbox("Category*", list(cat_map.keys()), key="fcat")
                date_found = st.date_input("Date Found*", value=date.today())
            with col2:
                loc_name     = st.selectbox("Location*", list(loc_map.keys()), key="floc")
                current_loc  = st.text_input("Current Storage Location", placeholder="e.g. Front desk, Room 101")
            submitted = st.form_submit_button("📦 Submit Found Report", use_container_width=True)

        if submitted:
            if not title or not description:
                st.error("Title and description are required.")
            else:
                payload = {
                    "title": title,
                    "description": description,
                    "dateFound": datetime.combine(date_found, datetime.min.time()).isoformat(),
                    "currentLocation": current_loc or None,
                    "category": {"categoryId": cat_map[cat_name]},
                    "location": {"locationId": loc_map[loc_name]},
                }
                uid = st.session_state.user["userId"]
                data, err = api("POST", f"/items/found?reporterId={uid}", json=payload)
                if err:
                    st.error(err)
                else:
                    st.success(f"✅ Found item reported! ID: {data.get('itemId')}")

# ══════════════════════════════════════════════════════════════════════════════
# PAGE: MY CLAIMS
# ══════════════════════════════════════════════════════════════════════════════
elif page == "📝 My Claims":
    st.markdown('<div class="section-header">My Claims</div>', unsafe_allow_html=True)
    uid = st.session_state.user["userId"]

    # Pre-fill if navigated from Browse
    prefill_item = st.session_state.pop("claim_item", None)

    with st.expander("➕ File a New Claim", expanded=bool(prefill_item)):
        found_items, _ = api("GET", "/items/found")
        found_items = [i for i in (found_items or []) if i.get("status") == "PENDING"]

        if not found_items:
            st.info("No pending found items to claim right now.")
        else:
            item_map = {f"[{i['itemId']}] {i['title']}": i["itemId"] for i in found_items}
            default_idx = 0
            if prefill_item:
                key = f"[{prefill_item['itemId']}] {prefill_item['title']}"
                if key in item_map:
                    default_idx = list(item_map.keys()).index(key)

            with st.form("claim_form"):
                item_label  = st.selectbox("Select Item*", list(item_map.keys()), index=default_idx)
                proof_desc  = st.text_area("Proof of Ownership*",
                                           placeholder="Describe why this item belongs to you…", height=100)
                proof_doc   = st.text_input("Proof Document Path (optional)",
                                            placeholder="e.g. /uploads/receipt.pdf")
                submitted = st.form_submit_button("📩 Submit Claim", use_container_width=True)

            if submitted:
                if not proof_desc:
                    st.error("Proof description is required.")
                else:
                    item_id = item_map[item_label]
                    payload = {"proofDescription": proof_desc, "proofDocument": proof_doc or None}
                    data, err = api("POST", f"/claims?claimantId={uid}&itemId={item_id}", json=payload)
                    if err:
                        st.error(err)
                    else:
                        st.success(f"✅ Claim #{data.get('claimId')} submitted!")

    st.markdown("---")
    st.markdown("#### My Claim History")
    claims, err = api("GET", f"/claims/user/{uid}")
    if err:
        st.error(err)
    elif not claims:
        st.info("You haven't filed any claims yet.")
    else:
        for claim in claims[::-1]:
            item = claim.get("item") or {}
            st.markdown(f"""
            <div class="card">
                <div class="card-title">Claim #{claim.get('claimId')} — {item.get('title','—')}</div>
                <div class="card-meta">
                    Filed: {fmt_dt(claim.get('claimDate'))} &nbsp;|&nbsp;
                    {status_badge(claim.get('status',''))}
                </div>
                <div style="margin-top:8px;font-size:.88rem;color:#374151;">
                    <b>Proof:</b> {claim.get('proofDescription','—')}
                </div>
                {"<div style='margin-top:4px;font-size:.85rem;color:#6b7280;'><b>Admin Notes:</b> " + claim.get('adminNotes','') + "</div>" if claim.get('adminNotes') else ""}
            </div>""", unsafe_allow_html=True)

# ══════════════════════════════════════════════════════════════════════════════
# PAGE: MY PROFILE
# ══════════════════════════════════════════════════════════════════════════════
elif page == "👤 My Profile":
    st.markdown('<div class="section-header">My Profile</div>', unsafe_allow_html=True)
    u   = st.session_state.user
    uid = u["userId"]

    col_info, col_edit = st.columns(2)

    with col_info:
        st.markdown(f"""
        <div class="card">
            <div class="card-title">👤 {u['name']}</div>
            <div class="card-meta">
                📧 {u.get('email','—')}<br>
                📞 {u.get('phone','—')}<br>
                🔖 Role: {u.get('role','—')}<br>
                ✅ Active: {u.get('active', True)}
            </div>
        </div>""", unsafe_allow_html=True)

        # Reported items
        st.markdown("#### Items I Reported")
        lost_items,  _ = api("GET", "/items/lost")
        found_items, _ = api("GET", "/items/found")
        all_items = (lost_items or []) + (found_items or [])
        mine = [i for i in all_items if (i.get("reporter") or {}).get("userId") == uid]
        if mine:
            df = pd.DataFrame([{
                "ID": i["itemId"],
                "Title": i["title"],
                "Type": "LOST" if "dateLost" in i else "FOUND",
                "Status": i.get("status"),
                "Reported": fmt_dt(i.get("dateReported")),
            } for i in mine])
            st.dataframe(df, use_container_width=True, hide_index=True)
        else:
            st.info("No items reported yet.")

    with col_edit:
        st.markdown("#### ✏️ Update Profile")
        with st.form("profile_form"):
            name  = st.text_input("Name",  value=u.get("name",""))
            phone = st.text_input("Phone", value=u.get("phone",""))
            pw    = st.text_input("New Password (leave blank to keep current)", type="password")
            submitted = st.form_submit_button("Save Changes", use_container_width=True)

        if submitted:
            payload = {"name": name, "phone": phone}
            if pw:
                payload["password"] = pw
            data, err = api("PUT", f"/users/{uid}", json=payload)
            if err:
                st.error(err)
            else:
                st.session_state.user.update(data)
                st.success("Profile updated!")

# ══════════════════════════════════════════════════════════════════════════════
# PAGE: ADMIN PANEL
# ══════════════════════════════════════════════════════════════════════════════
elif page == "🛡️ Admin Panel":
    if st.session_state.user.get("role") != "ADMIN":
        st.error("Access denied.")
        st.stop()

    st.markdown('<div class="section-header">Admin Panel — Claims Management</div>', unsafe_allow_html=True)
    admin_id = st.session_state.user["userId"]

    tab_pending, tab_all = st.tabs(["⏳ Pending Claims", "📋 All Claims"])

    with tab_pending:
        claims, err = api("GET", "/claims/pending")
        if err:
            st.error(err)
        elif not claims:
            st.success("🎉 No pending claims!")
        else:
            for claim in claims:
                item      = claim.get("item")      or {}
                claimant  = claim.get("claimant")  or {}
                cid = claim["claimId"]

                with st.expander(f"Claim #{cid} — {item.get('title','?')} by {claimant.get('name','?')}"):
                    st.markdown(f"**Filed:** {fmt_dt(claim.get('claimDate'))}")
                    st.markdown(f"**Claimant:** {claimant.get('name','—')} ({claimant.get('email','—')})")
                    st.markdown(f"**Item:** {item.get('title','—')} (ID {item.get('itemId','')})")
                    st.markdown(f"**Proof:** {claim.get('proofDescription','—')}")
                    if claim.get("proofDocument"):
                        st.markdown(f"📎 `{claim['proofDocument']}`")

                    ca, cr = st.columns(2)
                    with ca:
                        if st.button("✅ Approve", key=f"approve_{cid}", use_container_width=True):
                            _, err = api("PATCH", f"/claims/{cid}/approve?adminId={admin_id}")
                            if err:
                                st.error(err)
                            else:
                                st.success("Claim approved!")
                                st.rerun()
                    with cr:
                        reason = st.text_input("Reject reason", key=f"reason_{cid}", placeholder="Optional reason")
                        if st.button("❌ Reject", key=f"reject_{cid}", use_container_width=True):
                            r = f"?adminId={admin_id}&reason={requests.utils.quote(reason or 'No reason provided')}"
                            _, err = api("PATCH", f"/claims/{cid}/reject{r}")
                            if err:
                                st.error(err)
                            else:
                                st.warning("Claim rejected.")
                                st.rerun()

    with tab_all:
        claims, err = api("GET", "/claims")
        if err:
            st.error(err)
        elif claims:
            rows = []
            for c in claims:
                item     = c.get("item")     or {}
                claimant = c.get("claimant") or {}
                rows.append({
                    "ID":       c.get("claimId"),
                    "Item":     item.get("title","—"),
                    "Claimant": claimant.get("name","—"),
                    "Status":   c.get("status","—"),
                    "Filed":    fmt_dt(c.get("claimDate")),
                })
            st.dataframe(pd.DataFrame(rows), use_container_width=True, hide_index=True)

    st.markdown("---")
    st.markdown('<div class="section-header">Item Status Management</div>', unsafe_allow_html=True)

    all_items_data = []
    for endpoint in ["/items/lost", "/items/found"]:
        items, _ = api("GET", endpoint)
        all_items_data.extend(items or [])

    if all_items_data:
        item_options = {f"[{i['itemId']}] {i['title']} ({i.get('status')})": i for i in all_items_data}
        sel = st.selectbox("Select Item to Update Status", list(item_options.keys()))
        status_options = ["PENDING", "VERIFIED", "MATCHED", "CLAIMED", "CLOSED"]
        new_status = st.selectbox("New Status", status_options)
        if st.button("🔄 Update Status"):
            iid = item_options[sel]["itemId"]
            _, err = api("PATCH", f"/items/{iid}/status?status={new_status}")
            if err:
                st.error(err)
            else:
                st.success(f"Status updated to {new_status}")
                st.rerun()

# ══════════════════════════════════════════════════════════════════════════════
# PAGE: MANAGE USERS
# ══════════════════════════════════════════════════════════════════════════════
elif page == "👥 Manage Users":
    if st.session_state.user.get("role") != "ADMIN":
        st.error("Access denied.")
        st.stop()

    st.markdown('<div class="section-header">User Management</div>', unsafe_allow_html=True)

    users, err = api("GET", "/users")
    if err:
        st.error(err)
    elif users:
        rows = [{
            "ID":      u.get("userId"),
            "Name":    u.get("name"),
            "Email":   u.get("email"),
            "Phone":   u.get("phone"),
            "Role":    u.get("role"),
            "Active":  u.get("active"),
            "Joined":  fmt_dt(u.get("createdAt")),
        } for u in users]
        st.dataframe(pd.DataFrame(rows), use_container_width=True, hide_index=True)

        st.markdown("---")
        st.markdown("#### Delete User")
        del_options = {f"[{u['userId']}] {u['name']} ({u['email']})": u["userId"] for u in users}
        sel_user = st.selectbox("Select User", list(del_options.keys()))
        if st.button("🗑️ Delete User", type="primary"):
            uid_del = del_options[sel_user]
            _, err = api("DELETE", f"/users/{uid_del}")
            if err:
                st.error(err)
            else:
                st.success("User deleted.")
                st.rerun()

# ══════════════════════════════════════════════════════════════════════════════
# PAGE: CATEGORIES & LOCATIONS
# ══════════════════════════════════════════════════════════════════════════════
elif page == "🗂️ Categories & Locations":
    if st.session_state.user.get("role") != "ADMIN":
        st.error("Access denied.")
        st.stop()

    st.markdown('<div class="section-header">Categories & Locations</div>', unsafe_allow_html=True)
    tab_cat, tab_loc = st.tabs(["📂 Categories", "📍 Locations"])

    with tab_cat:
        cats, _ = api("GET", "/categories")
        cats = cats or []
        if cats:
            st.dataframe(pd.DataFrame([{
                "ID": c["categoryId"], "Name": c["categoryName"],
                "Description": c.get("description", "—")
            } for c in cats]), use_container_width=True, hide_index=True)

        st.markdown("#### ➕ Add Category")
        with st.form("cat_form"):
            cat_name = st.text_input("Category Name*")
            cat_desc = st.text_area("Description", height=70)
            submitted = st.form_submit_button("Add Category")
        if submitted:
            if not cat_name:
                st.error("Name required.")
            else:
                data, err = api("POST", "/categories",
                                json={"categoryName": cat_name, "description": cat_desc})
                if err:
                    st.error(err)
                else:
                    st.success(f"Category '{cat_name}' created!")
                    st.rerun()

        if cats:
            st.markdown("#### 🗑️ Delete Category")
            del_map = {f"[{c['categoryId']}] {c['categoryName']}": c["categoryId"] for c in cats}
            sel_cat = st.selectbox("Select category to delete", list(del_map.keys()), key="del_cat")
            if st.button("Delete Category"):
                _, err = api("DELETE", f"/categories/{del_map[sel_cat]}")
                if err:
                    st.error(err)
                else:
                    st.success("Deleted.")
                    st.rerun()

    with tab_loc:
        locs, _ = api("GET", "/locations")
        locs = locs or []
        if locs:
            st.dataframe(pd.DataFrame([{
                "ID": l["locationId"], "Place": l["placeName"],
                "City": l.get("city","—"), "State": l.get("state","—"),
                "Address": l.get("address","—")
            } for l in locs]), use_container_width=True, hide_index=True)

        st.markdown("#### ➕ Add Location")
        with st.form("loc_form"):
            c1, c2 = st.columns(2)
            with c1:
                place   = st.text_input("Place Name*")
                address = st.text_input("Address")
                city    = st.text_input("City")
            with c2:
                state   = st.text_input("State")
                postal  = st.text_input("Postal Code")
                loc_desc = st.text_area("Description", height=69)
            submitted = st.form_submit_button("Add Location")
        if submitted:
            if not place:
                st.error("Place name required.")
            else:
                payload = {"placeName": place, "description": loc_desc,
                           "address": address, "city": city,
                           "state": state, "postalCode": postal}
                data, err = api("POST", "/locations", json=payload)
                if err:
                    st.error(err)
                else:
                    st.success(f"Location '{place}' added!")
                    st.rerun()

        if locs:
            st.markdown("#### 🗑️ Delete Location")
            del_map = {f"[{l['locationId']}] {l['placeName']}": l["locationId"] for l in locs}
            sel_loc = st.selectbox("Select location to delete", list(del_map.keys()), key="del_loc")
            if st.button("Delete Location"):
                _, err = api("DELETE", f"/locations/{del_map[sel_loc]}")
                if err:
                    st.error(err)
                else:
                    st.success("Deleted.")
                    st.rerun()
