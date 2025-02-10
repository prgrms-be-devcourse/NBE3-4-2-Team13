"use client";

import RequireAdmin from "@/lib/auth/components/RequireAdmin";
import ClientPage from "./ClientPage";

export default function Page() {
    return (
        <RequireAdmin>
            <ClientPage />
        </RequireAdmin>
    );
}