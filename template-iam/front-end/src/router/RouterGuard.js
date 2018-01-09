module.exports = function (iam) {
  let module = {};

  module.iam = iam;

  module.requireGuest = function (to, from, next) {
    // will stop the routing if isAuthenticated
    module.isAuthenticated(function (result) {
      next(!result);
    })
  };

  module.requireUser = function (to, from, next) {
    //isAuthenticated ? continue route
    //if not ? will login and come back
    module.isAuthenticated(function (result) {
      if (result) {
        next(result);
      } else {
        next({
          path: '/admin/login',
          query: {
            redirect: to.fullPath
          }
        })
      }
    });
  };
  module.isAuthenticated = function (callback) {
    module.iam.adminLogin()
      .done(function (info) {
        callback(true);
      })
      .fail(function () {
        callback(false);
      });
  };
  return module;
};
