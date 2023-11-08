const now = new Date();
const futureDate = new Date(now.getFullYear(), now.getMonth() + 6, now.getDate(), now.getHours(), now.getMinutes());
document.getElementById("close_date").value = futureDate.toISOString().slice(0, 16);
document.getElementById("close_date").min = new Date().toISOString().slice(0, 16);
