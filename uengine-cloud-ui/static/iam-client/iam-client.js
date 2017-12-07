/**
 * Created by Seungpil Park on 2016. 9. 6..
 */
var IAM = function (host, contextPath) {
  this.host = host;
  this.contextPath = contextPath;
  if (!host) {
    this.baseUrl = location.protocol + '//' + location.hostname + (location.port ? ':' + location.port : '');
  } else {
    this.baseUrl = this.host;
  }
  if (this.contextPath) {
    this.baseUrl = this.baseUrl + this.contextPath;
  }
  this.user = undefined;
  this.management = undefined;

  $(window).ajaxSend(function (e, xhr, options) {
    var token = localStorage.getItem('uengine-iam-access-token');
    var managementKey = localStorage.getItem('uengine-iam-management-key');
    var managementSecret = localStorage.getItem('uengine-iam-management-secret');
    var clientKey = localStorage.getItem('uengine-iam-client-key');
    var clientSecret = localStorage.getItem('uengine-iam-client-secret');
    xhr.setRequestHeader('Authorization', token);
    if (managementKey && managementSecret) {
      xhr.setRequestHeader('management-key', managementKey);
      xhr.setRequestHeader('management-secret', managementSecret);
    }
    if (clientKey && clientSecret) {
      xhr.setRequestHeader('client-key', clientKey);
      xhr.setRequestHeader('client-secret', clientSecret);
    }
  });
};
IAM.prototype = {
  logout: function () {
    localStorage.removeItem('uengine-iam-access-token');
    // localStorage.removeItem('uengine-iam-management-id');
    // localStorage.removeItem('uengine-iam-management-key');
    // localStorage.removeItem('uengine-iam-management-secret');
    // localStorage.removeItem('uengine-iam-client-key');
    // localStorage.removeItem('uengine-iam-client-secret');
  },
  setDefaultManagement: function (id, key, secret) {
    localStorage.setItem('uengine-iam-management-id', id);
    localStorage.setItem('uengine-iam-management-key', key);
    localStorage.setItem('uengine-iam-management-secret', secret);
  },
  getDefaultManagement: function () {
    return localStorage.getItem('uengine-iam-management-id');
  },
  setDefaultClient: function (key, secret) {
    localStorage.setItem('uengine-iam-client-key', key);
    localStorage.setItem('uengine-iam-client-secret', secret);
  },
  adminLogin: function (data) {
    var me = this;
    var username = data.username;
    var password = data.password;
    var deferred = $.Deferred();
    var promise = $.ajax({
      type: "POST",
      url: me.baseUrl + '/rest/v1/access_token',
      data: 'username=' + username + '&password=' + password,
      contentType: "application/x-www-form-urlencoded",
      dataType: "json"
    });
    promise.done(function (response) {
      if (response['access_token']) {
        console.log('login success');
        var token = response['access_token'];
        localStorage.setItem("uengine-iam-access-token", token);
        deferred.resolve(response);
      } else {
        console.log('login failed');
        localStorage.removeItem("uengine-iam-access-token");
        deferred.reject();
      }
    });
    promise.fail(function (response, status, errorThrown) {
      console.log('login failed', errorThrown, response.responseText);
      localStorage.removeItem("uengine-iam-access-token");
      deferred.reject(response);
    });
    return deferred.promise();
  },
  adminValidateToken: function () {
    console.log('Validating token...');
    var me = this;
    var token = localStorage.getItem("uengine-iam-access-token");
    var deferred = $.Deferred();
    var promise = $.ajax({
      type: "GET",
      url: me.baseUrl + '/rest/v1/token_info?authorization=' + token,
      dataType: "json",
      async: false
    });
    promise.done(function (response) {
      console.log('validateToken success');
      deferred.resolve(response);
    });
    promise.fail(function (response, status, errorThrown) {
      console.log('validateToken failed', errorThrown, response.responseText);
      deferred.reject(response);
    });
    return deferred.promise();
  },
  passwordCredentialsLogin: function (username, password, scope, token_type, claim) {
    var data = {
      username: username,
      password: password,
      scope: scope,
      token_type: token_type,
      claim: claim,
      client_id: localStorage.getItem('uengine-iam-client-key'),
      client_secret: localStorage.getItem('uengine-iam-client-secret'),
      grant_type: 'password'
    };
    for (var key in data) {
      if (!data[key]) {
        delete data[key];
      }
    }
    var query_string = $.param(data);
    var options = {
      type: "POST",
      url: '/oauth/access_token',
      data: query_string,
      contentType: "application/x-www-form-urlencoded",
      dataType: 'json',
      resolve: function (response) {
        if (response['access_token']) {
          var token = response['access_token'];
          localStorage.setItem("uengine-iam-access-token", token);
          return response;
        } else {
          console.log('login failed');
          localStorage.removeItem("uengine-iam-access-token");
          return response;
        }
      }
    };
    return this.send(options);
  },
  validateToken: function (token) {
    token = token ? token : localStorage.getItem("uengine-iam-access-token");
    var options = {
      type: "GET",
      url: '/oauth/token_info?access_token=' + token,
      dataType: "json",
      async: true
    };
    return this.send(options);
  },
  getManagements: function () {
    var options = {
      type: "GET",
      url: '/rest/v1/management',
      dataType: "json",
      async: false
    };
    return this.send(options);
  },
  createManagement: function (data) {
    var options = {
      type: "POST",
      url: '/rest/v1/management',
      data: JSON.stringify(data),
      contentType: "application/json",
      dataType: 'text',
      resolve: function (response, status, xhr) {
        var locationHeader = xhr.getResponseHeader('Location');
        return locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
      }
    };
    return this.send(options);
  },
  updateManagement: function (id, data) {
    var options = {
      type: "PUT",
      url: '/rest/v1/management/' + id,
      data: JSON.stringify(data),
      contentType: "application/json",
      dataType: "json"
    };
    return this.send(options);
  },
  deleteManagement: function (id) {
    var options = {
      type: "DELETE",
      url: '/rest/v1/management/' + id
    };
    return this.send(options);
  },
  getUser: function (id) {
    var options = {
      type: "GET",
      url: '/rest/v1/user/' + id,
      dataType: "json"
    };
    return this.send(options);
  },
  getUserSearch: function (searchKey, offset, limit) {
    var data = {
      offset: offset ? offset : 0,
      limit: limit ? limit : 100,
      audit: 'NONE'
    };
    var url = searchKey ? '/rest/v1/user/search/' + searchKey : '/rest/v1/user/pagination';
    var options = {
      type: "GET",
      url: url,
      dataType: 'json',
      data: data,
      resolve: function (response, status, xhr) {
        var total = parseInt(xhr.getResponseHeader('x-uengine-pagination-totalnbrecords'));
        var filtered = parseInt(xhr.getResponseHeader('x-uengine-pagination-maxnbrecords'));
        // console.log(total, filtered);
        return {
          data: response,
          total: total,
          filtered: filtered,
          offset: data.offset,
          limit: data.limit
        };
      }
    };
    return this.send(options);
  },
  createUser: function (data) {
    var options = {
      type: "POST",
      url: '/rest/v1/user',
      data: JSON.stringify(data),
      contentType: "application/json",
      dataType: 'text',
      resolve: function (response, status, xhr) {
        var locationHeader = xhr.getResponseHeader('Location');
        return locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
      }
    };
    return this.send(options);
  },
  updateUser: function (id, data) {
    var options = {
      type: "PUT",
      url: '/rest/v1/user/' + id,
      data: JSON.stringify(data),
      contentType: "application/json",
      dataType: "json"
    };
    return this.send(options);
  },
  deleteUser: function (id) {
    var options = {
      type: "DELETE",
      url: '/rest/v1/user/' + id
    };
    return this.send(options);
  },
  createUserAvatarByFormData: function (file, contentType, id, userName) {
    var formData = new FormData();
    if (id) {
      formData.append('id', id);
    }
    if (userName) {
      formData.append('userName', userName);
    }
    formData.append('contentType', contentType);
    formData.append('file', file);
    var options = {
      type: "POST",
      url: '/rest/v1/avatar/formdata',
      data: formData,
      contentType: false,
      processData: false
    };
    return this.send(options);
  },
  deleteUserAvatarByUserName: function (userName) {
    var options = {
      type: "DELETE",
      url: '/rest/v1/avatar?userName=' + userName
    };
    return this.send(options);
  },
  deleteUserAvatarById: function (id) {
    var options = {
      type: "DELETE",
      url: '/rest/v1/avatar?id=' + id
    };
    return this.send(options);
  },
  getUserAvatarUrlById: function (id) {
    return this.baseUrl + '/rest/v1/avatar?id=' + id;
  },
  getUserAvatarUrlByUserName: function (userName) {
    return this.baseUrl + '/rest/v1/avatar?userName=' + userName;
  },
  signUp: function (redirect_url, data) {
    var options = {
      type: "POST",
      url: '/rest/v1/user/signup?redirect_url=' + redirect_url,
      data: JSON.stringify(data),
      contentType: "application/json",
      dataType: 'json'
    };
    return this.send(options);
  },
  signUpVerification: function (token) {
    var options = {
      type: "GET",
      url: '/rest/v1/user/signup/verification?token=' + token,
      dataType: "json"
    };
    return this.send(options);
  },
  signUpAccept: function (token) {
    var data = {
      token: token
    };
    var options = {
      type: "POST",
      url: '/rest/v1/user/signup/accept',
      data: JSON.stringify(data),
      contentType: "application/json",
      dataType: 'json'
    };
    return this.send(options);
  },

  forgotPassword: function (redirect_url, userName) {
    var data = {
      userName: userName
    };
    var options = {
      type: "POST",
      url: '/rest/v1/user/forgot?redirect_url=' + redirect_url,
      data: JSON.stringify(data),
      contentType: "application/json",
      dataType: 'json'
    };
    return this.send(options);
  },
  forgotPasswordVerification: function (token) {
    var options = {
      type: "GET",
      url: '/rest/v1/user/forgot/verification?token=' + token,
      dataType: "json"
    };
    return this.send(options);
  },
  forgotPasswordAccept: function (token, password) {
    var data = {
      token: token,
      password: password
    };
    var options = {
      type: "POST",
      url: '/rest/v1/user/forgot/accept',
      data: JSON.stringify(data),
      contentType: "application/json",
      dataType: 'json'
    };
    return this.send(options);
  },

  getClient: function (id) {
    var options = {
      type: "GET",
      url: '/rest/v1/client/' + id,
      dataType: "json"
    };
    return this.send(options);
  },
  getClientSearch: function (searchKey, offset, limit) {
    var data = {
      offset: offset ? offset : 0,
      limit: limit ? limit : 100,
      audit: 'NONE'
    };
    var url = searchKey ? '/rest/v1/client/search/' + searchKey : '/rest/v1/client/pagination';
    var options = {
      type: "GET",
      url: url,
      dataType: 'json',
      data: data,
      resolve: function (response, status, xhr) {
        var total = parseInt(xhr.getResponseHeader('x-uengine-pagination-totalnbrecords'));
        var filtered = parseInt(xhr.getResponseHeader('x-uengine-pagination-maxnbrecords'));
        return {
          data: response,
          total: total,
          filtered: filtered,
          offset: data.offset,
          limit: data.limit
        };
      }
    };
    return this.send(options);
  },
  createClient: function (data) {
    var options = {
      type: "POST",
      url: '/rest/v1/client',
      data: JSON.stringify(data),
      contentType: "application/json",
      dataType: 'text',
      resolve: function (response, status, xhr) {
        var locationHeader = xhr.getResponseHeader('Location');
        return locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
      }
    };
    return this.send(options);
  },
  updateClient: function (id, data) {
    var options = {
      type: "PUT",
      url: '/rest/v1/client/' + id,
      data: JSON.stringify(data),
      contentType: "application/json",
      dataType: "json"
    };
    return this.send(options);
  },
  deleteClient: function (id) {
    var options = {
      type: "DELETE",
      url: '/rest/v1/client/' + id
    };
    return this.send(options);
  },
  getClientScopes: function (clientId) {
    var options = {
      type: "GET",
      url: '/rest/v1/client/' + clientId + '/scope',
      dataType: "json"
    };
    return this.send(options);
  },
  getClientScope: function (clientId, scopeId) {
    var options = {
      type: "GET",
      url: '/rest/v1/client/' + clientId + '/scope/' + scopeId,
      dataType: "json"
    };
    return this.send(options);
  },
  createClientScope: function (clientId, scopeId) {
    var options = {
      type: "POST",
      url: '/rest/v1/client/' + clientId + '/scope/' + scopeId,
      contentType: "application/json",
      dataType: 'text',
      resolve: function (response, status, xhr) {
        var locationHeader = xhr.getResponseHeader('Location');
        return locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
      }
    };
    return this.send(options);
  },
  deleteClientScope: function (clientId, scopeId) {
    var options = {
      type: "DELETE",
      url: '/rest/v1/client/' + clientId + '/scope/' + scopeId
    };
    return this.send(options);
  },
  getAllScope: function () {
    var options = {
      type: "GET",
      url: '/rest/v1/scope',
      dataType: "json"
    };
    return this.send(options);
  },
  getScope: function (id) {
    var options = {
      type: "GET",
      url: '/rest/v1/scope/' + id,
      dataType: "json"
    };
    return this.send(options);
  },
  getScopeSearch: function (searchKey, offset, limit) {
    var data = {
      offset: offset ? offset : 0,
      limit: limit ? limit : 100,
      audit: 'NONE'
    };
    var url = searchKey ? '/rest/v1/scope/search/' + searchKey : '/rest/v1/scope/pagination';
    var options = {
      type: "GET",
      url: url,
      dataType: 'json',
      data: data,
      resolve: function (response, status, xhr) {
        var total = parseInt(xhr.getResponseHeader('x-uengine-pagination-totalnbrecords'));
        var filtered = parseInt(xhr.getResponseHeader('x-uengine-pagination-maxnbrecords'));
        return {
          data: response,
          total: total,
          filtered: filtered,
          offset: data.offset,
          limit: data.limit
        };
      }
    };
    return this.send(options);
  },
  createScope: function (data) {
    var options = {
      type: "POST",
      url: '/rest/v1/scope',
      data: JSON.stringify(data),
      contentType: "application/json",
      dataType: 'text',
      resolve: function (response, status, xhr) {
        var locationHeader = xhr.getResponseHeader('Location');
        return locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
      }
    };
    return this.send(options);
  },
  updateScope: function (id, data) {
    var options = {
      type: "PUT",
      url: '/rest/v1/scope/' + id,
      data: JSON.stringify(data),
      contentType: "application/json",
      dataType: "json"
    };
    return this.send(options);
  },
  deleteScope: function (id) {
    var options = {
      type: "DELETE",
      url: '/rest/v1/scope/' + id
    };
    return this.send(options);
  },
  getNotificationConfig: function (clientId) {
    var options = {
      type: "GET",
      url: '/rest/v1/client/' + clientId + '/notification_config',
      dataType: 'json'
    };
    return this.send(options);
  },
  updateNotificationConfig: function (clientId, data) {
    var options = {
      type: "POST",
      url: '/rest/v1/client/' + clientId + '/notification_config',
      data: JSON.stringify(data),
      contentType: "application/json",
      dataType: 'text'
    };
    return this.send(options);
  },

  getAllTemplate: function (clientId) {
    var options = {
      type: "GET",
      url: '/rest/v1/client/' + clientId + '/template',
      dataType: 'json'
    };
    return this.send(options);
  },

  createTemplate: function (clientId, template_type, locale, data) {
    var options = {
      type: "POST",
      url: '/rest/v1/client/' + clientId + '/template/' + template_type + '/' + locale,
      data: JSON.stringify(data),
      contentType: "application/json",
      dataType: 'text'
    };
    return this.send(options);
  },

  deleteTemplate: function (clientId, template_type, locale) {
    var options = {
      type: "DELETE",
      url: '/rest/v1/client/' + clientId + '/template/' + template_type + '/' + locale
    };
    return this.send(options);
  },

  setDefaultTemplate: function (clientId, template_type, locale) {
    var options = {
      type: "PUT",
      url: '/rest/v1/client/' + clientId + '/template/' + template_type + '/' + locale,
      dataType: 'text'
    };
    return this.send(options);
  },
  send: function (options) {
    var caller = arguments.callee.caller.name;
    var me = this;
    var deferred = $.Deferred();
    var ajaxOptions = {
      type: options.type,
      url: me.baseUrl + options.url
    };
    if (options.processData || typeof options.processData == 'boolean') {
      ajaxOptions.processData = options.processData;
    }

    if (options.dataType) {
      ajaxOptions.dataType = options.dataType;
    }
    if (options.contentType || typeof options.contentType == 'boolean') {
      ajaxOptions.contentType = options.contentType;
    }
    if (typeof options.async == 'boolean' && !options.async) {
      ajaxOptions.async = false;
    }
    if (options.data) {
      ajaxOptions.data = options.data;
    }
    var promise = $.ajax(ajaxOptions);
    promise.done(function (response, status, xhr) {
      console.log(caller + ' success');
      if (options.resolve) {
        response = options.resolve(response, status, xhr);
      }
      deferred.resolve(response);
    });
    promise.fail(function (response, status, errorThrown) {
      console.log(caller + ' failed');
      console.log(response, status, errorThrown);
      if (options.reject) {
        response = options.reject(response, status, errorThrown);
      }
      deferred.reject(response);
    });
    return deferred.promise();
  }
}
;
IAM.prototype.constructor = IAM;
