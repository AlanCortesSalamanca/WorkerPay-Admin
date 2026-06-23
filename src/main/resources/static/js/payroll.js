document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("[data-payroll-confirm]").forEach((form) => {
        form.addEventListener("submit", (event) => {
            const action = form.getAttribute("data-payroll-confirm");
            if (!window.confirm(`Confirma ${action}?`)) {
                event.preventDefault();
            }
        });
    });
});
