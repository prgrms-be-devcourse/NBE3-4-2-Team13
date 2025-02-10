import RequireAuthenticated from "@/lib/auth/components/RequireAuthenticated";
import PostCreatePage from "./CreatePage";

function Page() {
  return (
    <div className="container mx-auto p-4">
      <RequireAuthenticated>
        <PostCreatePage />
      </RequireAuthenticated>
    </div>
  );
}

export default Page;
