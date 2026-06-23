document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("[data-debt-confirm]").forEach((form) => {
        form.addEventListener("submit", (event) => {
            const action = form.getAttribute("data-debt-confirm");
            if (!window.confirm(`Confirma ${action} esta deuda?`)) {
                event.preventDefault();
            }
        });
    });
});
