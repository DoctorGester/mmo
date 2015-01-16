import core.board.ClientBoard
import core.board.ClientCell
import core.board.ClientSpell
import core.graphics.scenes.BattleScene
import core.graphics.scenes.Scenes;
import program.main.SceneUtil
import shared.board.Board
import shared.board.Cell
import shared.board.Spell
import shared.board.Unit

def onCheck(Spell spell, Board board, Cell target){
    target.getContentsType() == Cell.CONTENTS_UNIT && target.getUnit().getState() != Unit.STATE_DEAD
}

def onCheckAOE(Spell spell, Board board, Cell from, Cell to){
    from == to
}

def onCastBegin(ClientSpell spell, Board board, ClientCell target){
	spell.caster.setFacing(target.unit)
	1.0f
}

def onCastEnd(Spell spell, ClientBoard board, Cell target){
    def attach = SceneUtil.getScene(Scenes.BATTLE, BattleScene.class).getSpatialByUnit(target.unit).node

	target.unit.doHeal(6)
	board.nextTurn()
	spell.putOnCoolDown()

    board.addEffect("heal-effect", attach)
}