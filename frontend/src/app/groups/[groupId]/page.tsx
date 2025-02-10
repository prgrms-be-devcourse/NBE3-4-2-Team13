'use client';

import RequireAuthenticated from '@/lib/auth/components/RequireAuthenticated';
import ClientPage from './ClientPage';

interface Props {
  params: {
    groupId: string;
  };
}

export default function GroupDetailPage({ params }: Props) {
  console.log('GroupId in page:', params.groupId); // 디버깅용
  return (
    <RequireAuthenticated>
      <ClientPage groupId={params.groupId} />
    </RequireAuthenticated>
  );
}
