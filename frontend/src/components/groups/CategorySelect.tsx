'use client';

import { useEffect, useState } from 'react';

interface CategorySelectProps {
  value: string;
  onChange: (value: string) => void;
  className?: string;
}

export default function CategorySelect({ value, onChange, className = '' }: CategorySelectProps) {
  const [categories, setCategories] = useState<string[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/v1/categories', {
          headers: {
            Accept: 'application/json',
          },
          credentials: 'include',
        });

        if (!response.ok) {
          throw new Error('카테고리 목록을 불러오는데 실패했습니다.');
        }

        const data = await response.json();
        if (data.isSuccess && data.data) {
          setCategories(data.data);
        } else {
          throw new Error(data.message || '카테고리 목록을 불러오는데 실패했습니다.');
        }
      } catch (error) {
        console.error('Failed to fetch categories:', error);
        setError(error instanceof Error ? error.message : '카테고리 목록을 불러오는데 실패했습니다.');
      } finally {
        setIsLoading(false);
      }
    };

    fetchCategories();
  }, []);

  if (isLoading) {
    return <div className='animate-pulse bg-gray-200 dark:bg-gray-700 h-10 rounded' />;
  }

  if (error) {
    return <div className='text-red-500 dark:text-red-400'>{error}</div>;
  }

  return (
    <select
      value={value}
      onChange={(e) => onChange(e.target.value)}
      className={`w-full p-2 border rounded dark:bg-gray-700 dark:border-gray-600 dark:text-white ${className}`}
      required
    >
      <option value=''>카테고리 선택</option>
      {categories.map((category) => (
        <option key={category} value={category}>
          {category}
        </option>
      ))}
    </select>
  );
}
