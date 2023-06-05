import Body from "./body";

export default async function Home() {
  console.log("process.env: ", process.env);

  return (
    <main className="flex min-h-screen flex-col items-center justify-between p-24">
      <div>
        <h1>Formation OpenID</h1>
        <h2>Front-Office Next.js</h2>
      </div>
      <Body />
    </main>
  );
}
