import uvicorn
from anti_timpa.config import CFG

uvicorn.run("anti_timpa.app:app", host=CFG.server_host, port=CFG.server_port)
