/**
 * Notification Center Module
 */
const NotificationsManager = {
  async updateUnreadCount() {
    if (!Auth.isAuthenticated()) return;
    try {
      const count = await api.get('/notifications/unread-count');
      const badge = document.getElementById('nav-unread-badge');
      if (badge) {
        if (count > 0) {
          badge.textContent = count > 99 ? '99+' : count;
          badge.style.display = 'inline-block';
        } else {
          badge.style.display = 'none';
        }
      }
    } catch (e) {
      // Quiet fail for badge polling
    }
  },

  async loadNotificationsList(containerId = 'notifications-list') {
    const container = document.getElementById(containerId);
    if (!container) return;

    try {
      container.innerHTML = '<div style="text-align: center; padding: 2rem; color: #64748b;">Loading alerts...</div>';
      const items = await api.get('/notifications');

      if (!items || items.length === 0) {
        container.innerHTML = `
          <div class="card" style="text-align: center; padding: 3rem;">
            <div style="font-size: 0.85rem; font-weight: 700; color: #64748b; text-transform: uppercase; margin-bottom: 0.5rem;">Inbox Empty</div>
            <h3 style="font-size: 1.1rem; color: #0f172a; margin-bottom: 0.25rem;">No notifications</h3>
            <p style="color: #64748b; font-size: 0.9rem;">You are all caught up!</p>
          </div>
        `;
        return;
      }

      container.innerHTML = items.map(n => `
        <div class="card" style="margin-bottom: 1rem; border-left: 4px solid ${n.isRead ? '#cbd5e1' : '#1e3a8a'}; background: ${n.isRead ? '#ffffff' : '#f8fafc'};">
          <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem;">
            <div>
              <div style="display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.25rem;">
                <h4 style="font-weight: 700; color: #0f172a; font-size: 1rem;">${n.title}</h4>
                ${!n.isRead ? '<span class="badge badge-normal" style="font-size: 0.65rem;">NEW</span>' : ''}
                <span class="badge badge-${(n.type || 'INFO').toLowerCase()}">${n.type}</span>
              </div>
              <p style="color: #475569; font-size: 0.925rem; margin-bottom: 0.5rem;">${n.message}</p>
              <span style="font-size: 0.75rem; color: #94a3b8;">${UI.formatDate(n.createdAt)}</span>
            </div>
            <div>
              ${!n.isRead ? `<button onclick="NotificationsManager.markAsRead(${n.id})" class="btn btn-outline btn-sm">Mark Read</button>` : ''}
            </div>
          </div>
        </div>
      `).join('');
    } catch (err) {
      container.innerHTML = `<div class="alert alert-danger">Error loading notifications: ${err.message}</div>`;
    }
  },

  async markAsRead(id) {
    try {
      await api.put(`/notifications/${id}/read`);
      this.updateUnreadCount();
      this.loadNotificationsList();
      UI.showToast('Notification marked as read', 'success');
    } catch (e) {
      UI.showToast(e.message, 'error');
    }
  },

  async markAllAsRead() {
    try {
      await api.put('/notifications/read-all');
      this.updateUnreadCount();
      this.loadNotificationsList();
      UI.showToast('All notifications marked as read', 'success');
    } catch (e) {
      UI.showToast(e.message, 'error');
    }
  }
};
