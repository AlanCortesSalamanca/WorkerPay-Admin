document.addEventListener("DOMContentLoaded", () => {
    const salaryInput = document.querySelector("#baseSalary");
    if (salaryInput) {
        salaryInput.addEventListener("blur", () => {
            if (salaryInput.value !== "") {
                salaryInput.value = Number(salaryInput.value).toFixed(2);
            }
        });
    }

    document.querySelectorAll("[data-confirm-action]").forEach((form) => {
        form.addEventListener("submit", (event) => {
            const action = form.dataset.confirmAction;
            const confirmed = window.confirm(`Confirma que deseas ${action} este trabajador.`);
            if (!confirmed) {
                event.preventDefault();
            }
        });
    });
});
