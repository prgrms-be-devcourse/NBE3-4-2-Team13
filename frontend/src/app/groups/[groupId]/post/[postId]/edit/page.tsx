import RequireAuthenticated from "@/lib/auth/components/RequireAuthenticated";
import PostEditPage from "./EditPage";

function Page() {
  return (
    <div className="container mx-auto p-4">
      <RequireAuthenticated>
        <PostEditPage />
      </RequireAuthenticated>
    </div>
  );
}

export default Page;
