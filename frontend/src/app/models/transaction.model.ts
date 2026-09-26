export interface Transaction {
  id: number;
  accountId: number;
  transactionType: string;
  amount: number;
  description: string;
  reference: string;
  createdAt: string;
}
