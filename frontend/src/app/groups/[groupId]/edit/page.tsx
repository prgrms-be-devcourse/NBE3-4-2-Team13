'use client';

import ClientPage from './ClientPage';

export default function Page({ params }: { params: { groupId: string } }) {
  return <ClientPage groupId={params.groupId} />;
}
