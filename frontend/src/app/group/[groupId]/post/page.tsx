import RequireAuthenticated from "@/lib/auth/components/RequireAuthenticated";
import PostListPage from "./PostList";

function Page() {
  return (
    <div className="container mx-auto p-4">
      <RequireAuthenticated>
        <PostListPage />
      </RequireAuthenticated>
    </div>
  );
}

export default Page;
