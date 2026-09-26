import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login';
import { DashboardComponent } from './components/dashboard/dashboard';
import { AccountComponent } from './components/account/account';
import { TransactionsComponent } from './components/transactions/transactions';
import { DepositComponent } from './components/deposit/deposit';
import { WithdrawComponent } from './components/withdraw/withdraw';
import { TransferComponent } from './components/transfer/transfer';
import { AdminComponent } from './components/admin/admin';
import { UpiComponent } from './components/upi/upi';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'upi', component: UpiComponent, canActivate: [authGuard] },
  { path: 'account', component: AccountComponent, canActivate: [authGuard] },
  { path: 'transactions', component: TransactionsComponent, canActivate: [authGuard] },
  { path: 'deposit', component: DepositComponent, canActivate: [authGuard] },
  { path: 'withdraw', component: WithdrawComponent, canActivate: [authGuard] },
  { path: 'transfer', component: TransferComponent, canActivate: [authGuard] },
  { path: 'admin', component: AdminComponent, canActivate: [authGuard] },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];
