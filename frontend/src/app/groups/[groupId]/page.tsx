"use client";

import { use } from "react";
import RequireAuthenticated from "@/lib/auth/components/RequireAuthenticated";
import ClientPage from "./ClientPage";
import PostPreviewPage from "./PostPreviewPage";

interface Props {
  params: Promise<{
    groupId: string;
  }>;
}

export default function GroupDetailPage({ params }: Props) {
  const resolvedParams = use(params);
  console.log("GroupId in page:", resolvedParams.groupId); // 디버깅용

  return (
    <RequireAuthenticated>
      <ClientPage groupId={resolvedParams.groupId} />
      <PostPreviewPage groupId={resolvedParams.groupId}></PostPreviewPage>
    </RequireAuthenticated>
  );
}
