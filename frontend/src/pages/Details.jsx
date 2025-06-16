import React from "react";
import { useEffect } from "react";
import { Stack, Paper, Typography } from "@mui/material";
import { useParams } from 'react-router-dom';
import axios from 'axios';

export default function Details() {
    const { entityId } = useParams();
    
    const [data, setData] = React.useState(null);
    
    useEffect(() => {
        axios.get('http://127.0.0.1:8080/entity/' + entityId).then((response) => {
            setData(response.data);
            console.log(response.data);
        });
    }, [entityId]);

    return (
        <Stack 
            direction="column"
            justifyContent="center"
            spacing={3}
            sx={{
                padding: 4
            }}
        >
            <Typography variant="h5">
                {data && data.entity.name}
            </Typography>

            <Typography variant="body1">
                {data && data.entity.description}
            </Typography>

            {data && data.parameters.length > 0 &&
                <Stack direction="row" spacing={2}>
                    <Typography variant="body1">
                        <strong>Parameters:</strong>
                    </Typography>
                    <Stack direction="column" spacing={1.5}>
                        { data.parameters.map(param =>
                            <Stack direction="column">
                                <Typography variant="body1">
                                    <strong>{param.name}</strong> : {param.datatype}, default = {param.defaultValue}
                                </Typography>
                                <Typography variant="body2" sx={{ paddingLeft: 2 }}>
                                    {param.description}
                                </Typography>
                            </Stack>
                        )}
                    </Stack>
                </Stack>
            }

            {data && data.attributes.length > 0 &&
                <Stack direction="row" spacing={2}>
                    {data && data.attributes && 
                        <Typography variant="body1">
                            <strong>Attributes:</strong>
                        </Typography>
                    }
                    {data && data.attributes && 
                        <Stack direction="column" spacing={1.5}>
                            {data && data.attributes.map(attr =>
                                <Stack direction="column">
                                    <Typography variant="body1">
                                        <strong>{attr.name}</strong> : {attr.datatype}, default = {attr.defaultValue}
                                    </Typography>
                                    <Typography variant="body2" sx={{ paddingLeft: 2 }}>
                                        {attr.description}
                                    </Typography>
                                </Stack>
                            )}
                        </Stack>
                    }
                </Stack>
            }

            {data && data.returns.length > 0 &&
                <Stack direction="row" spacing={2}>
                    <Typography variant="body1">
                        <strong>Returns:</strong>
                    </Typography>
                    <Stack direction="column" spacing={1.5}>
                        {data && data.returns.map(param =>
                            <Stack direction="column">
                                <Typography variant="body1">
                                    <strong>{param.name}</strong> : {param.datatype}, default = {param.defaultValue}
                                </Typography>
                                <Typography variant="body2" sx={{ paddingLeft: 2 }}>
                                    {param.description}
                                </Typography>
                            </Stack>
                        )}
                    </Stack>
                </Stack>
            }

        </Stack>
    )
}