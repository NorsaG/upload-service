db.createUser({
  user: "appuser",
  pwd: "app_pass",
  roles: [{ role: "readWrite", db: "upload" }]
});