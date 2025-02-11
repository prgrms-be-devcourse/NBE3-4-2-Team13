'use client';

import { use } from 'react';
import ClientPage from './ClientPage';
import RequireAuthenticated from '@/lib/auth/components/RequireAuthenticated';

interface Props {
  params: Promise<{
    groupId: string;
  }>;
}

export default function GroupEditPage({ params }: Props) {
  const resolvedParams = use(params);

  return (
    <RequireAuthenticated>
      <ClientPage groupId={resolvedParams.groupId} />
    </RequireAuthenticated>
  );
}
