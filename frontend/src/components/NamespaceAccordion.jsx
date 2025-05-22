import React from "react";
import { styled } from '@mui/material/styles';
import ArrowForwardIosSharpIcon from '@mui/icons-material/ArrowForwardIosSharp';
import MuiAccordion from '@mui/material/Accordion';
import MuiAccordionSummary, {
  accordionSummaryClasses,
} from '@mui/material/AccordionSummary';
import MuiAccordionDetails from '@mui/material/AccordionDetails';
import Typography from '@mui/material/Typography';
import { NavLink } from "react-router";
import { Stack } from "@mui/material";

const Accordion = styled((props) => (
    <MuiAccordion disableGutters elevation={0} square {...props} />
))(({ theme }) => ({
    // border: `1px solid ${theme.palette.divider}`,
    '&:not(:last-child)': {
        borderBottom: 0,
    },
    '&::before': {
        display: 'none',
    },
}));
  
const AccordionSummary = styled((props) => (
    <MuiAccordionSummary
        expandIcon={<ArrowForwardIosSharpIcon sx={{ fontSize: '0.9rem' }} />}
        {...props}
    />
))(({ theme }) => ({
    backgroundColor: 'rgba(0, 0, 0, .03)',
    flexDirection: 'row-reverse',
    [`& .${accordionSummaryClasses.expandIconWrapper}.${accordionSummaryClasses.expanded}`]:
        {
            transform: 'rotate(90deg)',
        },
    [`& .${accordionSummaryClasses.content}`]: {
        marginLeft: theme.spacing(1),
    },
    ...theme.applyStyles('dark', {
        backgroundColor: 'rgba(255, 255, 255, .05)',
    }),
}));
  
const AccordionDetails = styled(MuiAccordionDetails)(({ theme }) => ({
    padding: theme.spacing(2),
    borderTop: '1px solid rgba(0, 0, 0, .125)',
}));

export default function NamespaceAccordion({ data }) {
    const [expanded, setExpanded] = React.useState('panel1');

    const handleChange = (panel) => (event, newExpanded) => {
        setExpanded(newExpanded ? panel : false);
    };

    return (
        <div>
            { data != null ?
                Object.entries(data).map(([namespace, details], key) =>
                    <Accordion expanded={expanded === 'panel' + key} onChange={handleChange('panel' + key)} key={key}>
                        <AccordionSummary aria-controls="panel1d-content" id="panel1d-header">
                            <Typography component="span">{namespace}</Typography>
                        </AccordionSummary>
                        <AccordionDetails>
                            <Stack direction='column'>
                                { details.classes && details.classes.length > 0 &&
                                    details.classes.map((name, idx) =>
                                        <NavLink key={"cls-" + idx} to={`${namespace}.${name}`}>{name}</NavLink>
                                    )
                                }
                                { details.functions && details.functions.length > 0 &&
                                    details.functions.map((name, idx) =>
                                        <NavLink key={"fn-" + idx} to={`${namespace}.${name}`}>{name}</NavLink>
                                    )
                                }
                            </Stack>
                        </AccordionDetails>
                    </Accordion>
                )
                :
                <Typography variant="body1" />
            }
        </div>
    ); 
}