document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("[data-advance-confirm]").forEach((form) => {
        form.addEventListener("submit", (event) => {
            const action = form.dataset.advanceConfirm;
            const confirmed = window.confirm(`Confirma que deseas ${action} este adelanto.`);
            if (!confirmed) {
                event.preventDefault();
            }
        });
    });
});
