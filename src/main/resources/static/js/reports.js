document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".alert-dismissible").forEach((alert) => {
        alert.addEventListener("click", () => {
            alert.remove();
        });
    });
});
