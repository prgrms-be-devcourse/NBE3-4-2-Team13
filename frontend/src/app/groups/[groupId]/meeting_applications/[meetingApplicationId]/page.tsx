'use client';

import { use } from 'react';
import ClientPage from './ClientPage';
import React from 'react';
import RequireAuthenticated from '@/lib/auth/components/RequireAuthenticated';

interface Props {
  params: {
    groupId: string;
    meetingApplicationId: string;
  };
}

export default function Page({ params }: Props) {
  return <ClientPage groupId={params.groupId} meetingApplicationId={params.meetingApplicationId} />;
}
