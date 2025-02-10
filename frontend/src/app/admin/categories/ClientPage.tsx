"use client";

import { useEffect, useState } from "react";
import axios from "axios";
import { useRouter } from "next/navigation";
import { FaPen, FaTrash, FaCheck, FaTimes } from "react-icons/fa";

interface Category {
    id: number;
    name: string;
}

interface CategoryPage {
    categories: Category[];
    currentPage: number;
    totalPages: number;
    totalItems: number;
    pageSize: number;
}

export default function ClientPage() {
    const [categoryPage, setCategoryPage] = useState<CategoryPage | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [page, setPage] = useState(1);
    const [newCategory, setNewCategory] = useState("");
    const [creating, setCreating] = useState(false);
    const [editingCategory, setEditingCategory] = useState<Category | null>(null); // 수정할 카테고리 상태
    const router = useRouter();
    const [createErrorMessage, setCreateErrorMessage] = useState(""); // 카테고리 생성 오류 메시지
    const [editErrorMessage, setEditErrorMessage] = useState(""); // 카테고리 수정 오류 메시지
    const [isEditing, setEditing] = useState(false);

    // 카테고리 목록 불러오기
    const fetchCategories = async (targetPage = 1) => {
        try {
            const token = localStorage.getItem("accessToken");
            if (!token) throw new Error("인증 토큰이 없습니다.");

            setLoading(true);
            const response = await axios.get(
                `http://localhost:8080/api/v1/admin/categories?page=${targetPage - 1}&size=10`,
                {
                    headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
                    withCredentials: true,
                }
            );

            if (response.data.isSuccess) {
                setCategoryPage(response.data.data);
            } else {
                throw new Error(response.data.message || "데이터를 불러오지 못했습니다.");
            }
        } catch (error) {
            setError(error instanceof Error ? error.message : "알 수 없는 오류 발생");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCategories(page);
    }, [page]);


    // 카테고리 삭제 함수
    const deleteCategory = async (categoryId: number) => {
        if (!window.confirm("정말 삭제하시겠습니까?")) return;

        try {
            const token = localStorage.getItem("accessToken");
            if (!token) throw new Error("인증 토큰이 없습니다.");

            await axios.delete(`http://localhost:8080/api/v1/admin/categories/${categoryId}`, {
                headers: { Authorization: `Bearer ${token}` },
                withCredentials: true,
            });

            alert("카테고리가 삭제되었습니다.");
            setCategoryPage((prev) =>
                prev
                    ? {
                        ...prev,
                        categories: prev.categories.filter((cat) => cat.id !== categoryId),
                        totalItems: prev.totalItems - 1,
                    }
                    : null
            );
        } catch (error) {
            alert(error instanceof Error ? error.message : "알 수 없는 오류 발생");
        }
    };

    // 카테고리 생성 함수
    const createCategory = async () => {
        if (!newCategory.trim()) {
            setCreateErrorMessage("카테고리 이름을 입력하세요.");
            return;
        }

        try {
            setCreating(true);
            setCreateErrorMessage("");

            const token = localStorage.getItem("accessToken");
            if (!token) throw new Error("인증 토큰이 없습니다.");

            const response = await axios.post(
                "http://localhost:8080/api/v1/admin/categories",
                { name: newCategory },
                {
                    headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
                    withCredentials: true,
                }
            );

            if (response.data.isSuccess) {
                alert("카테고리가 생성되었습니다.");
                setNewCategory(""); // 입력 필드 초기화
                setPage(1); // 첫 페이지로 이동
                fetchCategories(1);
            } else {
                throw new Error(response.data.message || "카테고리 생성 실패");
            }
        } catch (error) {
            if (axios.isAxiosError(error) && error.response) {
                const errorCode = error.response.data.code;
                switch (errorCode) {
                    case "C001":
                        setCreateErrorMessage("카테고리 이름은 필수입니다.");
                        break;
                    case "C002":
                        setCreateErrorMessage("카테고리 이름은 최대 10자까지 가능합니다.");
                        break;
                    case "C003":
                        setCreateErrorMessage("이미 존재하는 카테고리입니다.");
                        break;
                    default:
                        setCreateErrorMessage("카테고리 생성 중 오류가 발생했습니다.");
                }
            } else {
                setCreateErrorMessage("서버와의 통신 오류가 발생했습니다.");
            }
        } finally {
            setCreating(false);
        }
    };

    // 카테고리 수정 함수
    const updateCategory = async () => {
        if (!editingCategory) return;
        if (!editingCategory.name.trim()) {
            setEditErrorMessage("카테고리명을 입력하세요.");
            return;
        }

        try {
            setEditing(true);
            setEditErrorMessage("");

            const token = localStorage.getItem("accessToken");
            if (!token) throw new Error("인증 토큰이 없습니다.");

            const response = await axios.patch(
                `http://localhost:8080/api/v1/admin/categories/${editingCategory.id}`,
                { name: editingCategory.name },
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json", // ✅ 여기에 JSON 명시
                        Accept: "application/json", // ✅ JSON 응답을 받도록 명시
                    },
                    withCredentials: true,
                }
            );

            if (response.data.isSuccess) {
                alert("카테고리가 수정되었습니다.");
                setCategoryPage((prev) =>
                    prev
                        ? {
                            ...prev,
                            categories: prev.categories.map((cat) =>
                                cat.id === editingCategory.id ? { ...cat, name: editingCategory.name } : cat
                            ),
                        }
                        : null
                );
                setEditingCategory(null);
            } else {
                throw new Error(response.data.message || "카테고리 수정 실패");
            }
        } catch (error) {
            if (axios.isAxiosError(error) && error.response) {
                const responseData = error.response.data;
                const errorCode = responseData?.code;

                if (!responseData || Object.keys(responseData).length === 0) {
                    console.error("❗ 백엔드 응답이 비어 있음.");
                    setEditErrorMessage("서버에서 올바른 응답을 받지 못했습니다.");
                    return;
                }

                switch (errorCode) {
                    case "C001":
                        setEditErrorMessage("카테고리 이름은 필수입니다.");
                        break;
                    case "C002":
                        setEditErrorMessage("카테고리 이름은 최대 10자까지 가능합니다.");
                        break;
                    case "C003":
                        setEditErrorMessage("이미 존재하는 카테고리입니다.");
                        break;
                    default:
                        setEditErrorMessage(responseData?.message || "카테고리 수정 중 오류가 발생했습니다.");
                }
            } else {
                console.error("⛔ Axios 오류가 아닌 예외 발생:", error);
                setEditErrorMessage("서버와의 통신 오류가 발생했습니다.");
            }
        } finally {
            setEditing(false);
        }
    };

    return (
        <div className="min-h-screen bg-gray-50">
            {/* 상단 네비게이션 바 */}
            <div className="bg-white shadow">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between h-16 items-center">
                        <h1 className="text-xl font-bold text-gray-900">관리자 대시보드</h1>
                        <div className="flex space-x-4">
                            <button
                                onClick={() => router.push('/admin/members')}
                                className="px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-md hover:bg-gray-100"
                            >
                                회원 관리
                            </button>
                            <button
                                onClick={() => router.push('/admin/categories')}
                                className="px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-md hover:bg-gray-100"
                            >
                                카테고리 관리
                            </button>
                        </div>
                    </div>
                </div>
            </div>
            <div className="w-full max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8 flex gap-8">
                {/* 카테고리 목록 */}
                <div className="w-2/3 bg-white shadow rounded-lg ml-auto">
                    <div className="px-4 py-5 sm:px-6 border-b border-gray-200">
                        <h2 className="text-lg font-medium text-gray-900">카테고리 목록</h2>
                    </div>

                    {loading && <p className="p-4">로딩 중...</p>}
                    {error && <p className="p-4 text-red-600">{error}</p>}

                    {!loading && !error && categoryPage && (
                        <>
                            <div className="overflow-x-auto">
                                <table className="min-w-full divide-y divide-gray-200">
                                    <thead className="bg-gray-100">
                                    <tr>
                                        <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase w-1/6">
                                            ID
                                        </th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                                            카테고리명
                                        </th>
                                        <th className="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase w-1/6">
                                            관리
                                        </th>
                                    </tr>
                                    </thead>
                                    <tbody className="bg-white divide-y divide-gray-200">
                                    {categoryPage.categories.length > 0 ? (
                                        categoryPage.categories.map((category) => (
                                            <tr key={category.id} className="hover:bg-gray-50">
                                                <td className="px-6 py-4 text-sm text-gray-900">{category.id}</td>
                                                <td className="px-6 py-4 text-sm text-gray-900">
                                                    {editingCategory?.id === category.id ? (
                                                        <>
                                                            <input
                                                                type="text"
                                                                value={editingCategory.name}
                                                                onChange={(e) =>
                                                                    setEditingCategory({
                                                                        ...editingCategory,
                                                                        name: e.target.value
                                                                    })
                                                                }
                                                                className="border p-1"
                                                            />
                                                            {editErrorMessage && (
                                                                <p className="mt-2 text-red-500 text-sm">{editErrorMessage}</p>
                                                            )}
                                                        </>
                                                    ) : (
                                                        category.name
                                                    )}
                                                </td>
                                                <td className="px-6 py-4 text-sm flex justify-center items-center space-x-2">
                                                    {editingCategory?.id === category.id ? (
                                                        <>
                                                            <button
                                                                onClick={updateCategory}
                                                                className="w-7 h-7 flex items-center justify-center bg-green-600 text-white rounded-md hover:bg-green-700"
                                                            >
                                                                <FaCheck/>
                                                            </button>
                                                            <button
                                                                onClick={() => setEditingCategory(null)}
                                                                className="w-7 h-7 flex items-center justify-center bg-gray-400 text-white rounded-md hover:bg-gray-500"
                                                            >
                                                                <FaTimes/>
                                                            </button>
                                                        </>
                                                    ) : (
                                                        <>
                                                            <button
                                                                onClick={() => {
                                                                    const isConfirmed = window.confirm("해당 카테고리와 연결된 그룹이 있습니다. 그래도 수정하시겠습니까?");
                                                                    if (isConfirmed) {
                                                                        setEditingCategory(category);
                                                                    }
                                                                }}
                                                                className="w-7 h-7 flex items-center justify-center bg-white text-green-600 rounded-md hover:bg-green-600 hover:text-white border-none"
                                                            >
                                                                <FaPen/>
                                                            </button>
                                                            <button
                                                                onClick={() => {
                                                                    const isConfirmed = window.confirm("해당 카테고리와 연결된 그룹이 있습니다. 그래도 삭제하시겠습니까?");
                                                                    if (isConfirmed) {
                                                                        deleteCategory(category.id);
                                                                    }
                                                                }}
                                                                className="w-7 h-7 flex items-center justify-center bg-white text-red-600 rounded-md hover:bg-red-600 hover:text-white border-none"
                                                            >
                                                                <FaTrash/>
                                                            </button>
                                                        </>
                                                    )}
                                                </td>
                                            </tr>
                                        ))
                                    ) : (
                                        <tr>
                                            <td colSpan={3} className="px-6 py-4 text-center text-gray-500">
                                                카테고리가 없습니다.
                                            </td>
                                        </tr>
                                    )}
                                    </tbody>
                                </table>
                            </div>
                            <div className="p-4 flex justify-between items-center">
                                <button
                                    onClick={() => setPage((prev) => Math.max(prev - 1, 1))}
                                    disabled={categoryPage.currentPage === 1}
                                    className="px-4 py-2 bg-gray-200 rounded-md disabled:opacity-50"
                                >
                                    이전
                                </button>
                                <p className="text-sm text-gray-700">
                                    {categoryPage.totalPages > 0 ? categoryPage.currentPage : 1} / {Math.max(categoryPage.totalPages, 1)}
                                </p>
                                <button
                                    onClick={() => setPage((prev) => Math.min(prev + 1, categoryPage.totalPages))}
                                    disabled={categoryPage.currentPage === categoryPage.totalPages}
                                    className="px-4 py-2 bg-gray-200 rounded-md disabled:opacity-50"
                                >
                                    다음
                                </button>
                            </div>
                        </>
                    )}
                </div>

                {/* 카테고리 생성 컨테이너 */}
                <div className="w-1/3 bg-white shadow p-6 rounded-lg">
                    <h2 className="text-lg font-medium text-gray-900">새 카테고리 추가</h2>
                    <div className="mt-4">
                        <label className="block text-sm font-medium text-gray-700"></label>
                    </div>
                    <input
                        type="text"
                        value={newCategory}
                        onChange={(e) => setNewCategory(e.target.value)}
                        placeholder="카테고리 이름 입력"
                        className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />

                    {/* 🔴 에러 메시지 표시 (input 아래) */}
                    {createErrorMessage && (
                        <p className="mt-2 text-red-500">{createErrorMessage}</p>
                    )}

                    <button
                        onClick={createCategory}
                        disabled={creating}
                        className="mt-4 w-full px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-blue-300"
                    >
                        {creating ? "생성 중..." : "카테고리 추가"}
                    </button>

                </div>
            </div>
        </div>
    );
}