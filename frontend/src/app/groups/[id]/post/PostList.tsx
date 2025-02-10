"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { useRouter, useParams } from "next/navigation";
import { getPosts } from "@/api/post/postapi";
import { Plus } from "lucide-react";

const postStatusOptions = ["ALL", "PUBLIC", "PRIVATE", "NOTICE"];

export default function PostListPage() {
  const params = useParams();
  const router = useRouter();
  const groupId = Number(params.id);
  const token = localStorage.getItem("accessToken") || "";
  const [posts, setPosts] = useState<any[]>([]);
  const [postStatus, setPostStatus] = useState("ALL");
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    async function fetchPosts() {
      setLoading(true);
      try {
        if (!token) {
          router.push("/");
          return;
        }
        const data = await getPosts(groupId, search, postStatus, page, token);
        setPosts(data.content || []);
        setTotalPages(data.totalPages || 0);
      } catch (error) {
        console.error("Error fetching posts:", error);
      } finally {
        setLoading(false);
      }
    }
    fetchPosts();
  }, [groupId, postStatus, search, page, token]);

  return (
    <div className="container mx-auto p-6 relative">
      <div className="mb-10 text-center relative">
        <h1 className="text-4xl font-extrabold text-gray-800 mb-4">게시글</h1>
        {/* 글작성 버튼 */}
        <div className="absolute top-0 right-0">
          <Link
            href={`/groups/${groupId}/post/create`}
            className="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-full text-gray-700 hover:bg-gray-100 transition font-medium"
          >
            <Plus className="w-5 h-5" />
            글작성
          </Link>
        </div>
      </div>

      {/* 검색  */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between mb-8 gap-4">
        {/* 필터 버튼 */}
        <div className="flex justify-center space-x-3">
          {postStatusOptions.map((status) => (
            <button
              key={status}
              onClick={() => {
                setPostStatus(status);
                setPage(0);
              }}
              className={`px-3 py-1 rounded-full transition-colors shadow-sm ${
                postStatus === status
                  ? "bg-gray-700 text-white"
                  : "bg-gray-100 text-gray-700 hover:bg-gray-200"
              }`}
            >
              {status}
            </button>
          ))}
        </div>

        {/* 검색 입력란 */}
        <div className="flex justify-center">
          <input
            type="text"
            placeholder="검색어를 입력하세요..."
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(0);
            }}
            className="px-3 py-1 border rounded-full focus:outline-none focus:ring focus:ring-gray-400 text-sm"
          />
        </div>
      </div>

      {/* 게시글 목록 */}

      {loading ? (
        <p className="text-center text-gray-500 text-base">로딩 중...</p>
      ) : posts.length > 0 ? (
        <div className="flex flex-col gap-6 divide-y divide-gray-200">
          {posts.map((post) => (
            <Link
              key={post.postId}
              href={`/groups/${groupId}/post/${post.postId}`}
            >
              <div className="block bg-white p-4 rounded-lg hover:bg-gray-50 transition cursor-pointer">
                <h2 className="text-xl font-bold text-gray-800 mb-1">
                  {post.title}
                </h2>
                <div className="text-xs text-gray-500 mb-1">
                  작성자: {post.nickName} <br />
                  작성일: {post.createdAt}
                </div>
              </div>
            </Link>
          ))}
        </div>
      ) : (
        <p className="text-center text-gray-500 text-base">
          게시글이 없습니다.
        </p>
      )}

      {totalPages > 1 && (
        <div className="mt-8 flex justify-center items-center space-x-4">
          <button
            onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
            disabled={page === 0}
            className="px-3 py-1 rounded-full bg-gray-200 text-gray-700 disabled:opacity-50 text-sm"
          >
            이전
          </button>
          <span className="text-gray-700 text-sm">
            {page + 1} / {totalPages}
          </span>
          <button
            onClick={() =>
              setPage((prev) => Math.min(prev + 1, totalPages - 1))
            }
            disabled={page === totalPages - 1}
            className="px-3 py-1 rounded-full bg-gray-200 text-gray-700 disabled:opacity-50 text-sm"
          >
            다음
          </button>
        </div>
      )}
    </div>
  );
}
