const AuthClient = (() => {
    async function refreshSessionToken() {
        const response = await fetch('/rest/auth/refresh', {
            method: 'POST',
            credentials: 'same-origin'
        });

        if (response.status === 401) {
            throw new Error('Session expired. Please log in again.');
        }
    }

    async function sessionAwareFetch(url, options = {}) {
        try {
            await refreshSessionToken();
        } catch (error) {
            if (options.allowAnonymous !== true) {
                throw error;
            }
        }

        return fetch(url, {
            ...options,
            credentials: 'same-origin'
        });
    }

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
