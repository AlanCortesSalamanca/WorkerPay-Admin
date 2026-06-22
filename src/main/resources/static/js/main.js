document.addEventListener("DOMContentLoaded", () => {
    document.body.dataset.ready = "true";

    const sidebarToggle = document.querySelector("[data-sidebar-toggle]");
    if (sidebarToggle) {
        sidebarToggle.addEventListener("click", () => {
            document.body.classList.toggle("sidebar-open");
        });
    }

    document.querySelectorAll(".alert-dismissible").forEach((alert) => {
        alert.setAttribute("title", "Click para cerrar");
        alert.addEventListener("click", () => {
            alert.remove();
        });
    });
});
