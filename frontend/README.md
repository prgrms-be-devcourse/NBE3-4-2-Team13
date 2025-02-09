This is a [Next.js](https://nextjs.org) project bootstrapped with [`create-next-app`](https://nextjs.org/docs/app/api-reference/cli/create-next-app).

## Getting Started

First, run the development server:

```bash
npm run dev
# or
yarn dev
# or
pnpm dev
# or
bun dev
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.

You can start editing the page by modifying `app/page.tsx`. The page auto-updates as you edit the file.

This project uses [`next/font`](https://nextjs.org/docs/app/building-your-application/optimizing/fonts) to automatically optimize and load [Geist](https://vercel.com/font), a new font family for Vercel.

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.

You can check out [the Next.js GitHub repository](https://github.com/vercel/next.js) - your feedback and contributions are welcome!

## Deploy on Vercel

The easiest way to deploy your Next.js app is to use the [Vercel Platform](https://vercel.com/new?utm_medium=default-template&filter=next.js&utm_source=create-next-app&utm_campaign=create-next-app-readme) from the creators of Next.js.

Check out our [Next.js deployment documentation](https://nextjs.org/docs/app/building-your-application/deploying) for more details.

## 환경변수 설정 
1. ** `.env` 파일 생성 **  
   프로젝트 루트 폴더에 `process.env` 파일을 생성하고, 아래와 같이 환경변수를 등록합니다. 

   ```env
   # API 기본 URL 설정 
   REACT_APP_API_BASE_URL=http://localhost:8080/api/v1
    ``` 

2. `.env` 파일에 정의된 `REACT_APP_API_BASE_URL` 값은 프론트엔드 애플리케이션에서 API 요청을 보낼 때 기본 URL로 사용됩니다. 
3. 변경사항을 저장하고 애플리케이션을 다시 시작하면 환경변수가 적용됩니다.