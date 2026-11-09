import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = localStorage.getItem('basicAuth');
  if (auth && req.url.startsWith('/api')) {
    req = req.clone({
      setHeaders: { Authorization: `Basic ${auth}` },
    });
  }
  return next(req);
};
