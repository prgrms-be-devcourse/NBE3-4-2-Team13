'use client';

import RequireAuthenticated from '@/lib/auth/components/RequireAuthenticated';
import ClientPage from './ClientPage';

export default function Page() {
  return (
    <RequireAuthenticated>
      <ClientPage />
    </RequireAuthenticated>
  );
}
