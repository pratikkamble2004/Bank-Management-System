import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../core/services/auth.service';
import { map, take, switchMap, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.currentUser$.pipe(
    take(1),
    switchMap(user => {
      if (user) {
        if (state.url.startsWith('/admin') && user.role !== 'ADMIN') {
          return of(router.createUrlTree(['/dashboard']));
        }
        return of(true);
      }
      
      return authService.checkSession().pipe(
        map(res => {
          if (res && res.success) {
            const loggedInUser = authService.getCurrentUser();
            if (state.url.startsWith('/admin') && loggedInUser?.role !== 'ADMIN') {
              return router.createUrlTree(['/dashboard']);
            }
            return true;
          }
          return router.createUrlTree(['/login']);
        }),
        catchError(() => of(router.createUrlTree(['/login'])))
      );
    })
  );
};
