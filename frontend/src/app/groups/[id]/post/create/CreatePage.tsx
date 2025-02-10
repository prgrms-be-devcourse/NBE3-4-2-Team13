"use client";

import { useState, ChangeEvent, FormEvent } from "react";
import { useRouter, useParams } from "next/navigation";
import api from "@/api";
import { useFileUpload } from "../hooks/useFileUpload";

function PostCreatePage() {
  const params = useParams();
  const router = useRouter();
  const {
    files,
    isDragging,
    handleFileChange,
    handleDragOver,
    handleDragLeave,
    handleDrop,
    handleFileRemove,
  } = useFileUpload();

  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [postStatus, setPostStatus] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  // 게시글 save
  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    if (!title.trim() || !content.trim()) {
      setError("제목과 내용을 입력해주세요.");
      setLoading(false);
      return;
    }

    if (!postStatus) {
      setError("공개 여부를 선택해주세요.");
      setLoading(false);
      return;
    }

    const token = localStorage.getItem("accessToken");

    if (!token) {
      router.push("/login");
      return;
    }

    try {
      const formData = new FormData();
      const postData = {
        title,
        content,
        postStatus,
        groupId: Number(params.id),
      };

      formData.append(
        "post",
        new Blob([JSON.stringify(postData)], { type: "application/json" })
      );
      files.forEach((file) => formData.append("file", file));

      const response = await api.post("/post", formData, {
        headers: {
          "Content-Type": "multipart/form-data",
          Authorization: `Bearer ${token}`,
        },
      });

      const postId = response.data.data.postId;

      alert("게시글이 성공적으로 등록되었습니다!");
      router.push(`/groups/${params.id}/post/${postId}`);
    } catch (error: any) {
      setError("게시글 등록에 실패했습니다.");
      console.error("게시글 등록 오류:", error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto p-6 bg-white shadow-md rounded-lg">
      <h1 className="text-2xl font-bold text-center text-gray-800 mb-6">
        게시글 작성
      </h1>

      {error && <p className="text-red-500 text-sm mb-4">{error}</p>}

      <form onSubmit={handleSubmit} className="space-y-4">
        <input
          type="text"
          placeholder="제목을 입력하세요..."
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          className="w-full p-3 border rounded-md focus:ring focus:ring-blue-300"
        />

        <textarea
          rows={10}
          placeholder="내용을 입력하세요..."
          value={content}
          onChange={(e) => setContent(e.target.value)}
          className="w-full p-3 border rounded-md focus:ring focus:ring-blue-300"
        ></textarea>

        <div className="flex space-x-4">
          <label className="flex items-center space-x-2">
            <input
              type="radio"
              value="PUBLIC"
              checked={postStatus === "PUBLIC"}
              onChange={() => setPostStatus("PUBLIC")}
            />
            <span>일반</span>
          </label>

          <label className="flex items-center space-x-2">
            <input
              type="radio"
              value="PRIVATE"
              checked={postStatus === "PRIVATE"}
              onChange={() => setPostStatus("PRIVATE")}
            />
            <span>멤버</span>
          </label>

          <label className="flex items-center space-x-2">
            <input
              type="radio"
              value="NOTICE"
              checked={postStatus === "NOTICE"}
              onChange={() => setPostStatus("NOTICE")}
            />
            <span>공지</span>
          </label>
        </div>

        <div className="max-w-lg mx-auto p-4">
          <div
            className={`border-2 border-dashed p-6 rounded-lg text-center ${
              isDragging ? "border-blue-500 bg-blue-50" : "border-gray-300"
            }`}
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
          >
            <p className="text-gray-600">
              📂 파일을 여기로 드래그 앤 드롭하세요
            </p>
          </div>

          <input
            type="file"
            multiple
            onChange={handleFileChange}
            className="mt-4 w-full"
          />

          {files.length > 0 && (
            <ul className="mt-4 border p-2 rounded">
              {files.map((file, index) => (
                <li key={index} className="flex justify-between items-center">
                  <span>{file.name}</span>
                  <button
                    onClick={() => handleFileRemove(index)}
                    className="text-red-500 text-xs bg-gray-200 px-2 py-1 rounded"
                  >
                    삭제
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>

        <button
          type="submit"
          className={`w-full p-3 font-bold rounded-md border transition-colors ${
            loading || !title || !content || !postStatus
              ? "border-gray-300 text-gray-400 cursor-not-allowed"
              : "border-gray-400 text-gray-800 hover:bg-gray-50"
          }`}
          disabled={loading || !title || !content || !postStatus}
        >
          {loading ? "게시글 등록 중..." : "게시글 등록"}
        </button>
      </form>
    </div>
  );
}

export default PostCreatePage;
