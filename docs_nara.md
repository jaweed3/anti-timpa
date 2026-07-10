API Documentation | NaraRouter
NaraRouter
Home
Developer docs
Build on the NaraRouter API

One stable, OpenAI-compatible endpoint across leading models. Point any OpenAI SDK at our base URL, authenticate with your key, and ship.

On this page

    Quickstart
    Authentication & keys
    Chat completions
    Streaming
    Models
    Reasoning
    Rate limits & quotas
    Errors
    Plans & pricing

Quickstart

Three steps to your first response: create a key, set the base URL, send a request. The API speaks the OpenAI Chat Completions format, so existing OpenAI clients work by changing two lines.

1. Create an API key

Sign in and open the API keys page to mint a key. The secret is shown once at creation — copy it immediately and store it securely. Keys begin with the prefix sk-nry-.

2. Set the base URL

Send all requests to the gateway base URL. The chat endpoint lives at /v1/chat/completions.
text

https://router.bynara.id/v1

3. Send your first request

Use cURL or any OpenAI-compatible SDK. Pass a model alias (see Models), your messages, and your key as a Bearer token.

curl https://router.bynara.id/v1/chat/completions \
  -H "Authorization: Bearer sk-nry-xxxxxxxx" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "deepseek-3.2",
    "messages": [
      { "role": "user", "content": "Hello!" }
    ]
  }'

Because the API is OpenAI-compatible, the official OpenAI SDKs work unchanged apart from the base URL and key — no provider-specific client required.
Authentication & keys

Every request to the API must carry your secret key in the Authorization header as a Bearer token.

Authorization header
http

Authorization: Bearer sk-nry-xxxxxxxxxxxxxxxxxxxxxxxxxxxx

Key format

Keys are prefixed with sk-nry- followed by a random secret. The full secret is returned only once, at creation or rotation; afterwards only a masked form is shown.

Rotation & revocation

Manage keys from the dashboard. You can rotate a key (issue a new secret and invalidate the old one), revoke a key (it stays listed but no longer authenticates), or delete it. Rotated and revoked secrets stop working immediately.

Keep keys secret

Treat keys like passwords. Never embed them in client-side code, mobile apps, or public repositories. Use server-side environment variables and rotate any key you suspect is exposed.
Chat completions

Send a list of messages and receive a model response. This is the primary endpoint and follows the OpenAI Chat Completions schema.

Endpoint
http

POST https://router.bynara.id/v1/chat/completions

Request parameters

The gateway routes on the fields below; additional OpenAI-compatible fields in the body are forwarded to the model as-is.
Parameter	Type	Required	Description
model	

string
	Yes	

A model alias from the Models list. Determines routing and which plan tier may use it.
messages	

array
	Yes	

An array of message objects, each with a role (system, user, or assistant) and content. At least one message is required.
stream	

boolean
	

No
	

When true, the response is delivered as Server-Sent Events. When false (default), a single JSON object is returned.
temperature	

number
	

No
	

Sampling temperature. Forwarded to the model unchanged.
max_tokens	

integer
	

No
	

Upper bound on output tokens. Must be non-negative. Used to size the request; the model honors it as a ceiling.
reasoning_effort	

string
	

No
	

Thinking depth for reasoning models: low, medium, or high. Ignored by non-reasoning models. Forwarded to the model as-is; dropped for schema-strict upstreams.

Request
json

{
  "model": "deepseek-3.2",
  "messages": [
    { "role": "system", "content": "You are helpful." },
    { "role": "user", "content": "Hello!" }
  ],
  "temperature": 0.7,
  "max_tokens": 256
}

Response
json

{
  "id": "chatcmpl-...",
  "object": "chat.completion",
  "model": "deepseek-3.2",
  "choices": [
    {
      "index": 0,
      "message": { "role": "assistant", "content": "Hello! How can I help?" },
      "finish_reason": "stop"
    }
  ],
  "usage": { "prompt_tokens": 18, "completion_tokens": 8, "total_tokens": 26 }
}

On success you receive a standard chat completion object. The model field in the response is the public alias you requested.

The gateway validates model, messages, and max_tokens before forwarding. Streaming requests against a model that does not support streaming are rejected.
Streaming

Set stream to true to receive the response incrementally as Server-Sent Events (SSE). Each event carries a JSON delta in the OpenAI streaming format.

Wire format

The connection uses content-type text/event-stream. Each chunk arrives as a data: line containing a JSON delta. The stream terminates with a final data: [DONE] sentinel.
text

data: {"choices":[{"delta":{"content":"Hel"}}]}

data: {"choices":[{"delta":{"content":"lo"}}]}

data: [DONE]

Consuming the stream

Most OpenAI SDKs expose streaming natively. With the official SDK, iterate the streamed chunks and read each delta's content.
python

stream = client.chat.completions.create(
    model="deepseek-3.2",
    messages=[{"role": "user", "content": "Tell me a story."}],
    stream=True,
)

for chunk in stream:
    delta = chunk.choices[0].delta.content
    if delta:
        print(delta, end="", flush=True)

If an error occurs mid-stream, the gateway emits a clean error event followed by [DONE]; an error before the first byte is returned as a normal JSON error response instead.
Models

Pass a model alias in the model field. The list below is loaded live from the public plans endpoint, so it always reflects the models currently offered and which plan tier grants each.

Live from /api/plans. The authenticated /v1/models returns exactly the aliases your own plan entitles.
Model alias	Quota	Free	Hermes	Hermes Pro	Mimo Lite	Mimo Plus	Mimo Pro	Deepseek Lite	Deepseek Plus	Deepseek Pro	Kimi Fams	Claude Fams	GPT Fams
mistral-large	Base	
											
mistral-medium-3-5	Base	
											
tencent-hy3	Base	
											
mimo-v2.5-hermes			
	
									
mimo-v2.5-pro-hermes			
	
									
mimo-v2.5					
	
	
						
mimo-v2.5-pro-ultraspeed					
	
	
						
mimo-v2.5-pro					
	
	
						
deepseek-v4-flash								
	
	
			
deepseek-v4-pro								
	
	
			
kimi-k2.6											
		
kimi-k2.7-code											
		
claude-opus-4.7-plan												
	
claude-opus-4.8-plan												
	
claude-sonnet-5-plan												
	
gpt-5.4													
gpt-5.5													
gpt-5.6-luna													
gpt-5.6-sol													
gpt-5.6-terra													
claude-fable-5	PAYG only												
claude-opus-4.7	PAYG only												
claude-opus-4.8	PAYG only												
claude-sonnet-5	PAYG only												
claude-sonnet-5-bynara	PAYG only												
glm-5.2	PAYG only												
glm-5.2-plan	PAYG only												
kimi-k2.7-code-free	PAYG only												
minimax-m3	PAYG only												
claude-sonnet-4.5	PAYG only												
qwen3.7-max	PAYG only												

The Quota column shows which daily token quota a model draws from. So using a model only spends its own class quota.

Tiers are not strict supersets: Lite and Lite Mocin are sibling sets. A plan can reach a model class only if it grants at least one model in that class.

Some models are reasoning models (for example kimi-k2.5 and gemini-3.1-pro) that spend part of the token budget on internal reasoning before producing an answer. If you set a very low max_tokens, the visible content may come back empty because the budget was used up while reasoning. For these models, use a higher max_tokens.
Reasoning

Reasoning ("thinking") models spend part of the token budget on internal reasoning before answering. Control how deeply they think per request with reasoning_effort.

reasoning_effort

Higher effort means deeper thinking — better on hard tasks, but more output tokens and higher latency. On a non-reasoning model the field is ignored.
Effort	Thinking	Cost & speed	Use for
low
	

Short
	

Cheapest, fastest
	

Everyday chat, simple Q&A
medium
	

Moderate
	

Balanced
	

Most tasks — the sensible default
high
	

Long, deep
	

Most tokens, slowest
	

Hard math, multi-step coding, planning
json

{
  "model": "deepseek-v4-pro",
  "messages": [
    { "role": "user", "content": "Prove that sqrt(2) is irrational." }
  ],
  "reasoning_effort": "high"
}

Which models support it

Call /v1/models and check the reasoning flag on each model — true means it is a reasoning model. The Models catalog also shows a Reasoning badge.

Safe to always send

reasoning_effort is a no-op on non-reasoning models, so you can send it on every request without gating per model.

Output budget

For reasoning models the gateway guarantees enough output budget so thinking never starves the visible answer. A very low max_tokens can still return empty content on these models — leave room or omit it.

If you omit reasoning_effort, the provider's own default applies. When in doubt, medium is the best balance of quality, cost, and speed.
Rate limits & quotas

Limits depend on your plan. Subscription plans are governed by a per-minute request rate and a daily token quota; the free tier and per-model fair-use caps apply otherwise.
Plan	Request rate (per minute)	Daily token quota
Free	10 req/min	
Base: 5,000,000
Hermes	15 req/min	Fair use
Hermes Pro	50 req/min	Fair use
Mimo Lite	30 req/min	Fair use
Mimo Plus	40 req/min	Fair use
Mimo Pro	60 req/min	Fair use
Deepseek Lite	25 req/min	Fair use
Deepseek Plus	40 req/min	Fair use
Deepseek Pro	60 req/min	Fair use
Kimi Fams	30 req/min	Fair use
Claude Fams	25 req/min	Fair use
GPT Fams	25 req/min	Fair use

Request rate (per minute)

Each plan sets a maximum number of requests per minute. Exceeding it returns a 429 with a rate_limited error. The window resets every minute.

Daily token quota

Subscription plans count input + output tokens against a daily quota but the quota is per model class, not one account-wide ceiling. Each class (base, Lite, Mocin, Pro) has its own separate daily cap. When a class bucket is reached, only that class returns 429 until the next day; other class buckets keep working. A null cap means fair-use (no hard daily ceiling).

Per-class daily token quotas

Each model belongs to a quota class — base, Lite, Mocin, or Pro — and each class has its own daily token quota. A subscription gets a separate daily quota for every class it can access, and the quotas are independent: using a model only counts against its own class quota and never reduces another. When one quota is exhausted, models in the other classes you have still work until the quotas reset at the start of the next day.

Concurrency

Plans also bound how many requests may run at once. Excess concurrent requests are rejected with 429; retry once an in-flight request completes.

429 behavior

On any limit breach the response status is 429 with the rate_limited error type. A daily token breach is per model tier — the message reads that this model tier's quota is reached, while other model tiers you have access to still work. Back off and retry after the relevant window resets. Per-plan limits are shown on the pricing page and load live below.
Errors

Errors use a single, stable JSON envelope. Switch on the type field rather than parsing the message. The request_id helps correlate a failure with server logs.

Error shape
json

{
  "error": {
    "type": "rate_limited",
    "message": "Rate limit exceeded. Please retry later.",
    "request_id": "req_..."
  }
}

Status codes
Status	Type	Meaning
400	validation_error	

The request was malformed or failed validation (for example, a missing model or messages field).
401	unauthorized	

Missing or invalid API key. Check the Authorization header.
403	forbidden	

Authenticated, but your plan does not include the requested model, or the account is suspended.
404	not_found	

The requested model alias or endpoint does not exist.
413	bad_request	

The request body or input is too large.
415	unsupported_media_type	

Content-Type must be application/json.
429	rate_limited	

Rate limit or a per-model-tier daily token quota was exceeded. A quota breach affects only that model tier — other tiers you have access to still work. Retry after the window resets.
503	service_unavailable	

The model service is temporarily unavailable. Retry with backoff.
500	internal_error	

An unexpected internal error. Retry; if it persists, contact support with the request_id.
Plans & pricing

Pricing is in Rupiah, billed per day or per week. Each tier grants a model set, a request rate, and a daily token quota. Tiers load live below.

Free
Models: 3

Free

Hermes
Models: 2

IDR 1,000 / day

IDR 5,000 / week

Hermes Pro
Models: 2

IDR 2,000 / day

IDR 10,000 / week

Mimo Lite
Models: 3

IDR 5,000 / day

IDR 30,000 / week

Mimo Plus
Models: 3

IDR 8,000 / day

IDR 45,000 / week

Mimo Pro
Models: 3

IDR 10,000 / day

IDR 55,000 / week

Deepseek Lite
Models: 2

IDR 15,000 / day

IDR 88,000 / week

Deepseek Plus
Models: 2

IDR 25,000 / day

IDR 145,000 / week

Deepseek Pro
Models: 2

IDR 35,000 / day

IDR 199,000 / week

Kimi Fams
Models: 2

IDR 49,000 / day

Claude Fams
Models: 3

IDR 89,000 / day

GPT Fams
Models: 5

IDR 89,000 / day
NaraRouter

One stable API for model routing, usage tracking, subscriptions, and gateway controls.
Product
Features
Documentation
Pricing
Resources
API reference
Status
Changelog
Company
About
Contact
Legal
Terms of Service
Privacy Policy
Fair Use Policy
© 2026 byNara
All rights reserved.