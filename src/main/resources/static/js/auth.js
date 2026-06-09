const AuthClient = (() => {
    // Sends the browser back to login when the authenticated session can no longer be refreshed.
    function handleSessionExpired() {
        globalThis.location.assign('/login?expired');
    }

    // Calls the refresh endpoint before protected frontend actions.
    async function refreshSessionToken() {
        const response = await fetch('/rest/auth/refresh', {
            method: 'POST',
            credentials: 'same-origin'
        });

        if (response.status === 401) {
            handleSessionExpired();
            throw new Error('Session expired. Please log in again.');
        }
    }

    // Wraps fetch so protected requests try to refresh the session first.
    async function sessionAwareFetch(url, options = {}) {
        try {
            await refreshSessionToken();
        } catch (error) {
            if (options.allowAnonymous !== true) {
                throw error;
            }
        }

        const response = await fetch(url, {
            ...options,
            credentials: 'same-origin'
        });

        if (response.status === 401 && options.allowAnonymous !== true) {
            handleSessionExpired();
            throw new Error('Session expired. Please log in again.');
        }

        return response;
    }

    // Periodically refreshes the browser session while the page is open.
    function startAutoRefresh() {
        const fifteenMinutes = 15 * 60 * 1000;
        setInterval(() => {
            refreshSessionToken().catch(() => {});
        }, fifteenMinutes);
    }

    return {
        refreshSessionToken,
        sessionAwareFetch,
        startAutoRefresh
    };
})();
